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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;


/**
 * @author Gary O'Neall
 *
 * This singleton class is used to access the Conformance Questionnaire Git Repository (https://github.com/OpenChain-Project/conformance-questionnaire)
 * 
 * The following is a typical worflow:
 * - Refresh the repo by calling <code>refresh()</code>.  This will pull the latest from the repository
 * - Checkout a specific commit or tag by calling <code>File checkout(String tag, String commitRef)</code>
 * - Access the checked out files using the directory return value from checkout
 */
public class QuestionnaireGitRepo {
	
	public static final String QUESTIONNAIRE_PREFIX = "questionnaire";
	public static final String QUESTIONNAIRE_SUFFIX = ".json";
	
	public class QuestionnaireFileIterator implements Iterator<File> {
		
		private File[] jsonFiles;
		private int jsonFileIndex = 0;
		
		public QuestionnaireFileIterator(File repoDir) throws GitRepoException {
			if (repoDir == null) {
				logger.error("Null repository in new Questionnaire File Iterator");
				throw new GitRepoException("Unexpected error getting Questionnair files.  Please contact the OpenChain team with this error.");
			}
			if (!repoDir.exists()) {
				logger.error("Non existent directory for Questionnaire File Iterator: "+repoDir.getAbsolutePath());
				throw new GitRepoException("Unexpected error getting Questionnair files.  Please contact the OpenChain team with this error.");				
			}
			if (!repoDir.isDirectory()) {
				logger.error(repoDir.getAbsolutePath()+" is not a directory.");
				throw new GitRepoException("Unexpected error getting Questionnair files.  Please contact the OpenChain team with this error.");				
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
			throw new RuntimeException("Remove not supported for Questionnaire File Iterator");
		}
		
	}
	
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	public static final String REPO_DIR_NAME = "questionnaire_repo";
	static final Logger logger = Logger.getLogger(QuestionnaireGitRepo.class);
	//TODO Internationalize the error strings
	
	File workingDir = null;
	Repository repo = null;
	Git git = null;
	
	private static QuestionnaireGitRepo instance = null;

	public String QUESTIONAIRRE_URI = "https://github.com/OpenChain-Project/conformance-questionnaire.git";
	
	public static synchronized QuestionnaireGitRepo getQuestionnaireGitRepo() throws GitRepoException {
		if (instance == null) {
			instance = new QuestionnaireGitRepo();
		}
		return instance;
	}
	
	private QuestionnaireGitRepo() throws GitRepoException {
		openRepo();
	}
	
	/**
	 * Initializes properties for the class and refreshes or clones the repository to make it
	 * current
	 * @throws GitRepoException
	 */
	private void openRepo() throws GitRepoException {
		
		Path gitPath = FileSystems.getDefault().getPath(TMP_DIR).resolve(REPO_DIR_NAME);
		workingDir = gitPath.toFile();
		File gitDir = gitPath.resolve(".git").toFile();
		
		if (workingDir.exists()) {
			if (workingDir.isFile()) {
				// Hmmm, I guess we just delete this rather odd file
				logger.warn("Unexpected file foundin the git path.  Deleting "+gitPath.toString());
				if (!workingDir.delete()) {
					logger.error("Unable to delete unexpected file foundin the git path "+gitPath.toString());
					throw new GitRepoException("Unexpected error cleaning up the Git directory.  Please contact the OpenChain team with this error.");
				}
			} else if (workingDir.isDirectory()){
				// Check to see if this is already a repo
				try {
					RepositoryBuilder builder = new RepositoryBuilder().setGitDir(gitDir);
					repo = builder.build();
					String remoteUrl = repo.getConfig().getString( "remote", "origin", "url" );
					if (QUESTIONAIRRE_URI.equals(remoteUrl)) {
						// Matches the remote repo - we can just refresh the directory and return
						git = new Git(repo);
						refresh();
						return;
					} else {
						logger.warn("Found a directory other than the questionnaire repo - deleting and starting from scratch");
						repo.close();
						repo = null;
						deleteDirectory(workingDir);
					}
				} catch (Exception ex) {
					logger.warn("Error opening and checking existing repo - starting from scratch", ex);
					if (repo != null) {
						repo.close();
						repo = null;
						try {
							deleteDirectory(workingDir);
						} catch (IOException e) {
							logger.warn("Unable to delete git repository directory on error",e);
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
			logger.error("I/O Error cloning repository to directory "+gitPath.toString(), e);
			throw new GitRepoException("I/O error cloning repository", e);
		} catch (InvalidRemoteException e) {
			logger.error("Unable to access the github repository "+QUESTIONAIRRE_URI, e);
			throw new GitRepoException("Unable to access the github repository", e);
		} catch (TransportException e) {
			logger.error("Unable to access the github repository (transport error) "+QUESTIONAIRRE_URI, e);
			throw new GitRepoException("Unable to access the github repository", e);
		} catch (GitAPIException e) {
			logger.error("API error accessing the github repository "+QUESTIONAIRRE_URI, e);
			throw new GitRepoException("Unable to access the github repository", e);
		}
	}
	
	/**
	 * Deletes a directory including subdirectories
	 * @param dirOrFile
	 * @throws IOException
	 */
	private void deleteDirectory(File dirOrFile) throws IOException {
		if (dirOrFile.isFile()) {
			if (!dirOrFile.delete()) {
				throw new IOException("Unable to delete file "+dirOrFile.getAbsolutePath());
			}
		} else {
			File[] files = dirOrFile.listFiles();
			for (File f : files) {
				deleteDirectory(f);
			}
			if (!dirOrFile.delete()) {
				throw new IOException("Unable to delete directory "+dirOrFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Checkout a specific tag or commit reference.  If commitRef is not null, that will be used for
	 * checkout.  If tag is not null and commitRef is null, the tag will be used.  If both are null
	 * then "master" will be checked out.
	 * Note: you may want to call <code>refresh()</code> prior to checking out to make sure you have the latest
	 * @param tag Tag to checkout - may be null
	 * @param commitRef
	 * @throws GitRepoException 
	 */
	public synchronized void checkOut(String tag, String commitRef) throws GitRepoException
	{
		try {
			if (commitRef != null) {
				git.checkout().setName(commitRef).call();
			} else if (tag != null) {
				git.checkout().setName("refs/tags/"+tag).call();
			} else {
				git.checkout().setName("master").call();
			}
		} catch (RefAlreadyExistsException e) {
			logger.error("Unexpected ref already exists exception checking out",e);
			throw new GitRepoException("Unable to check out specific questionnaire version.  Please contact the OpenChain team with this error.");
		} catch (RefNotFoundException e) {
			logger.error("Reference not found checking out",e);
			if (commitRef != null) {
				throw new GitRepoException("Commit reference "+commitRef+" was not found.");
			} else if (tag != null) {
				throw new GitRepoException("Tag "+tag+" was not found.");
			} else {
				throw new GitRepoException("Unable to check out the master version.  Please contact the OpenChain team with this error.");
			}
		} catch (InvalidRefNameException e) {
			logger.error("Invalid ref name exception checking out",e);
			throw new GitRepoException("Unable to check out specific questionnaire version.  Please contact the OpenChain team with this error.");
		} catch (CheckoutConflictException e) {
			logger.error("Checkout conflict exception checking out",e);
			throw new GitRepoException("Unable to check out specific questionnaire version.  Please contact the OpenChain team with this error.");
		} catch (GitAPIException e) {
			logger.error("Unexpected GIT API exception checking out",e);
			throw new GitRepoException("Unable to check out specific questionnaire version.  Please contact the OpenChain team with this error.");
		}
	}
	
	/**
	 * Pulls the latest from the repository
	 * @throws GitRepoException 
	 */
	public synchronized void refresh() throws GitRepoException {
		try {
			git.pull().call();
		} catch (WrongRepositoryStateException e) {
			logger.error("Wrong repository exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (InvalidConfigurationException e) {
			logger.error("Invalid configuration exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (DetachedHeadException e) {
			logger.error("Detached Head exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (InvalidRemoteException e) {
			logger.error("Invalid remote exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (CanceledException e) {
			logger.error("Git pull unexpectededly cancelled",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (RefNotFoundException e) {
			logger.error("Ref not found exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (RefNotAdvertisedException e) {
			logger.error("Ref not advertised exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (NoHeadException e) {
			logger.error("No head exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (TransportException e) {
			logger.error("Transport exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
		} catch (GitAPIException e) {
			logger.error("GIT API exception refreshing repo",e);
			throw new GitRepoException("Unable to refresh the Questionnaire Git Repository.  Please contact the OpenChain team with this error.");
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
	 * @throws GitRepoException
	 */
	public synchronized Iterator<File> getQuestionnaireJsonFiles() throws GitRepoException {
		return new QuestionnaireFileIterator(this.workingDir);
	}

	/**
	 * Only used for test!  If the repository is open, deletes the repository directory and removes the instance
	 * @throws IOException 
	 */
	protected synchronized static void cleanupDelete() throws IOException {
		if (instance != null) {
			instance.repo.close();
			instance.deleteDirectory(instance.getDirectory());
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
}
