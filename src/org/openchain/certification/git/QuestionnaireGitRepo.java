/**
 * Copyright (c) 2018 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.openchain.certification.git;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.openchain.certification.I18N;


/**
 * @author Gary O'Neall
 *
 * This singleton class is used to access the Conformance Questionnaire Git Repository (https://github.com/OpenChain-Project/conformance-questionnaire)
 * 
 * The following is a typical worflow:
 * - Refresh the repo by calling <code>refresh()</code>.  This will pull the latest from the repository
 * - Lock the repository so that no one else will checkout a different version
 * - Checkout a specific commit or tag by calling <code>File checkout(String tag, String commitRef)</code>
 * - Access the checked out files using the directory return value from checkout
 * - Unlock the repository
 */
public class QuestionnaireGitRepo {
	
	public static final String QUESTIONNAIRE_PREFIX = "questionnaire";  //$NON-NLS-1$
	public static final String QUESTIONNAIRE_SUFFIX = ".json";  //$NON-NLS-1$
	
	public class QuestionnaireFileIterator implements Iterator<File> {
		
		private File[] jsonFiles;
		private int jsonFileIndex = 0;
		private String language;
		
		/**
		 * @param repoDir Directory containing the questionnaire repository
		 * @param language Language for the logged in user
		 * @throws GitRepoException
		 */
		public QuestionnaireFileIterator(File repoDir, String language) throws GitRepoException {
			this.language = language;
			if (repoDir == null) {
				logger.error("Null repository in new Questionnaire File Iterator");  //$NON-NLS-1$
				throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.0", language)); //$NON-NLS-1$
			}
			if (!repoDir.exists()) {
				logger.error("Non existent directory for Questionnaire File Iterator: "+repoDir.getAbsolutePath());  //$NON-NLS-1$
				throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.0", language));				 //$NON-NLS-1$
			}
			if (!repoDir.isDirectory()) {
				logger.error(repoDir.getAbsolutePath()+" is not a directory.");  //$NON-NLS-1$
				throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.0", language));				 //$NON-NLS-1$
			}
			jsonFiles = repoDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(QUESTIONNAIRE_PREFIX) && name.endsWith(QUESTIONNAIRE_SUFFIX);
				}
				
			});
		}

		@Override
		public boolean hasNext() {
			return jsonFiles.length > jsonFileIndex;
		}

		@Override
		public File next() {
			if (jsonFiles.length > jsonFileIndex) {
				return jsonFiles[jsonFileIndex++];				
			} else {
				return null;
			}
		}

		@Override
		public void remove() {
			throw new RuntimeException(I18N.getMessage("QuestionnaireGitRepo.3", language)); //$NON-NLS-1$
		}
		
	}
	
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");  //$NON-NLS-1$
	public static final String REPO_DIR_NAME = "questionnaire_repo";  //$NON-NLS-1$
	static final Logger logger = Logger.getLogger(QuestionnaireGitRepo.class);
	
	File workingDir = null;
	Repository repo = null;
	Git git = null;
	private ReentrantLock lock = new ReentrantLock();
	
	private static QuestionnaireGitRepo instance = null;

	public String QUESTIONAIRRE_URI = "https://github.com/OpenChain-Project/conformance-questionnaire.git";  //$NON-NLS-1$
	
	/**
	 * @param language Language for the logged in user
	 * @return The questionnaire git instance
	 * @throws GitRepoException
	 */
	public static synchronized QuestionnaireGitRepo getQuestionnaireGitRepo(String language) throws GitRepoException {
		if (instance == null) {
			instance = new QuestionnaireGitRepo(language);
		}
		return instance;
	}
	
	/**
	 * @param language Language for the logged in user
	 * @throws GitRepoException
	 */
	private QuestionnaireGitRepo(String language) throws GitRepoException {
		openRepo(language);
	}
	
	/**
	 * Initializes properties for the class and refreshes or clones the repository to make it
	 * current
	 * @param language Language for the logged in user
	 * @throws GitRepoException
	 */
	private void openRepo(String language) throws GitRepoException {
		
		Path gitPath = FileSystems.getDefault().getPath(TMP_DIR).resolve(REPO_DIR_NAME);
		workingDir = gitPath.toFile();
		File gitDir = gitPath.resolve(".git").toFile(); //$NON-NLS-1$
		
		if (workingDir.exists()) {
			if (workingDir.isFile()) {
				// Hmmm, I guess we just delete this rather odd file
				logger.warn("Unexpected file foundin the git path.  Deleting "+gitPath.toString());  //$NON-NLS-1$
				if (!workingDir.delete()) {
					logger.error("Unable to delete unexpected file foundin the git path "+gitPath.toString());  //$NON-NLS-1$
					throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.5", language)); //$NON-NLS-1$
				}
			} else if (workingDir.isDirectory()){
				// Check to see if this is already a repo
				try {
					RepositoryBuilder builder = new RepositoryBuilder().setGitDir(gitDir);
					repo = builder.build();
					String remoteUrl = repo.getConfig().getString( "remote", "origin", "url" );  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
					if (QUESTIONAIRRE_URI.equals(remoteUrl)) {
						// Matches the remote repo - we can just refresh the directory and return
						git = new Git(repo);
						refresh(language);
						return;
					} else {
						logger.warn("Found a directory other than the questionnaire repo - deleting and starting from scratch");  //$NON-NLS-1$
						repo.close();
						repo = null;
						deleteDirectory(workingDir, language);
					}
				} catch (Exception ex) {
					logger.warn("Error opening and checking existing repo - starting from scratch", ex);  //$NON-NLS-1$
					if (repo != null) {
						repo.close();
						repo = null;
						try {
							deleteDirectory(workingDir, language);
						} catch (IOException e) {
							logger.warn("Unable to delete git repository directory on error",e);  //$NON-NLS-1$
						}
					}
				}
			}
		}
		CloneCommand command = Git.cloneRepository();
		try {
			Files.createDirectory(gitPath);
			command.setDirectory(workingDir);
			command.setURI(QUESTIONAIRRE_URI);
			git = command.call();
			repo = git.getRepository();
		} catch (IOException e) {
			logger.error("I/O Error cloning repository to directory "+gitPath.toString(), e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.6", language), e); //$NON-NLS-1$
		} catch (InvalidRemoteException e) {
			logger.error("Unable to access the github repository "+QUESTIONAIRRE_URI, e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.7", language), e); //$NON-NLS-1$
		} catch (TransportException e) {
			logger.error("Unable to access the github repository (transport error) "+QUESTIONAIRRE_URI, e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.7", language), e); //$NON-NLS-1$
		} catch (GitAPIException e) {
			logger.error("API error accessing the github repository "+QUESTIONAIRRE_URI, e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.7", language), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Deletes a directory including subdirectories
	 * @param dirOrFile
	 * @param language Language for the logged in user
	 * @throws IOException
	 */
	private void deleteDirectory(File dirOrFile, String language) throws IOException {
		if (dirOrFile.isFile()) {
			if (!dirOrFile.delete()) {
				throw new IOException(I18N.getMessage("QuestionnaireGitRepo.10", language, dirOrFile.getAbsolutePath())); //$NON-NLS-1$
			}
		} else {
			File[] files = dirOrFile.listFiles();
			for (File f : files) {
				deleteDirectory(f, language);
			}
			if (!dirOrFile.delete()) {
				throw new IOException(I18N.getMessage("QuestionnaireGitRepo.11", language, dirOrFile.getAbsolutePath())); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Checkout a specific tag or commit reference.  If commitRef is not null, that will be used for
	 * checkout.  If tag is not null and commitRef is null, the tag will be used.  If both are null
	 * then "master" will be checked out.
	 * Note: you may want to call <code>refresh()</code> prior to checking out to make sure you have the latest
	 * @param tag Tag to checkout - may be null
	 * @param commitRef Commit reference tage - may be null
	 * @param language Language for the logged in user
	 * @throws GitRepoException 
	 */
	public synchronized void checkOut(String tag, String commitRef, String language) throws GitRepoException
	{
		try {
			if (commitRef != null && !commitRef.isEmpty()) {
				git.checkout().setName(commitRef).call();
			} else if (tag != null && !tag.isEmpty()) {
				git.checkout().setName("refs/tags/"+tag).call();  //$NON-NLS-1$
			} else {
				git.checkout().setName("master").call();  //$NON-NLS-1$
			}
		} catch (RefAlreadyExistsException e) {
			logger.error("Unexpected ref already exists exception checking out",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.12", language)); //$NON-NLS-1$
		} catch (RefNotFoundException e) {
			logger.error("Reference not found checking out",e);  //$NON-NLS-1$
			if (commitRef != null) {
				throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.13", language, commitRef)); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (tag != null) {
				throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.15", language, tag)); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.17", language)); //$NON-NLS-1$
			}
		} catch (InvalidRefNameException e) {
			logger.error("Invalid ref name exception checking out",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.18", language)); //$NON-NLS-1$
		} catch (CheckoutConflictException e) {
			logger.error("Checkout conflict exception checking out",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.19", language)); //$NON-NLS-1$
		} catch (GitAPIException e) {
			logger.error("Unexpected GIT API exception checking out",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.19", language)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Pulls the latest from the repository
	 * @param language Language for the logged in user
	 * @throws GitRepoException 
	 */
	public synchronized void refresh(String language) throws GitRepoException {
		try {
			String fullBranch = repo.getFullBranch();
			if (fullBranch == null || !fullBranch.startsWith("refs/")) {  //$NON-NLS-1$
				// we are in a detached head state
				checkOut(null, null, language);
				git.pull().call();
				checkOut(null, fullBranch, language);
			} else {
				git.pull().call();
			}
		} catch (WrongRepositoryStateException e) {
			logger.error("Wrong repository exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (InvalidConfigurationException e) {
			logger.error("Invalid configuration exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (DetachedHeadException e) {
			logger.error("Detached Head exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (InvalidRemoteException e) {
			logger.error("Invalid remote exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (CanceledException e) {
			logger.error("Git pull unexpectededly cancelled",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (RefNotFoundException e) {
			logger.error("Ref not found exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (RefNotAdvertisedException e) {
			logger.error("Ref not advertised exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (NoHeadException e) {
			logger.error("No head exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (TransportException e) {
			logger.error("Transport exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (GitAPIException e) {
			logger.error("GIT API exception refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		} catch (IOException e) {
			logger.error("I/O error refreshing repo",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.20", language)); //$NON-NLS-1$
		}
	}
	
	/**
	 * @return the root directory of the Questionnaire Repository
	 */
	public synchronized File getDirectory() {
		return this.workingDir;
	}
	
	/**
	 * @return an iterator which iterates through all questionnaire repository files
	 * @param language
	 * @throws GitRepoException
	 */
	public synchronized Iterator<File> getQuestionnaireJsonFiles(String language) throws GitRepoException {
		return new QuestionnaireFileIterator(this.workingDir, language);
	}

	/**
	 * Only used for test!  If the repository is open, deletes the repository directory and removes the instance
	 * @param language Language for the logged in user
	 * @throws IOException 
	 */
	protected synchronized static void cleanupDelete(String language) throws IOException {
		if (instance != null) {
			instance.repo.close();
			instance.deleteDirectory(instance.getDirectory(), language);
			instance = null;
		}
	}
	
	/**
	 * Set the instance to null - used for TEST ONLY!
	 */
	protected synchronized static void resetInstance() {
		instance.repo.close();
		instance = null;
	}

	/**
	 * @param language Language for the logged in user
	 * @return List of all tags in the repository
	 * @throws GitRepoException
	 */
	public String[] getTags(String language) throws GitRepoException {
		List<Ref> tags;
		try {
			tags = git.tagList().call();
			String[] retval = new String[tags.size()];
			int i = 0;
			int trimLen = "refs/tags/".length(); //$NON-NLS-1$
			for (Ref tag:tags) {
				retval[i++] = tag.getName().substring(trimLen);
			}
			return retval;
		} catch (GitAPIException e) {
			logger.error("Error getting GIT tags",e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.33", language)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Locks the repository preventing other threads from locking the repository until it is unlocked
	 */
	public void lock() {
		this.lock.lock();
	}
	
	/**
	 * Unlocks the repository
	 */
	public void unlock() {
		this.lock.unlock();
	}

	/**
	 * @param language Language for the logged in user
	 * @return The hash of the head commit for the repository
	 * @throws GitRepoException
	 */
	public String getHeadCommit(String language) throws GitRepoException {
		try {
			return this.repo.resolve(Constants.HEAD).getName();
		} catch (RevisionSyntaxException e) {
			logger.error("Invalid syntax getting commit head information", e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.34", language)); //$NON-NLS-1$
		} catch (AmbiguousObjectException e) {
			logger.error("Ambiguous object exception getting commit head information", e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.34", language)); //$NON-NLS-1$
		} catch (IncorrectObjectTypeException e) {
			logger.error("Incorrect object type exception getting commit head information", e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.34", language)); //$NON-NLS-1$
		} catch (IOException e) {
			logger.error("I/O error getting commit head information", e);  //$NON-NLS-1$
			throw new GitRepoException(I18N.getMessage("QuestionnaireGitRepo.34", language)); //$NON-NLS-1$
		}
	}
}
