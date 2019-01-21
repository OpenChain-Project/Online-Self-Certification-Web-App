This is the source code for the OpenChain Online Self-Certification WebApp.

The application is located at https://certification.openchainproject.org

The application is primarily driven from a database where all questions and all
answers are stored.

The UI uses jQuery and a jQuery UI front-end.  The primary style sheet is located 
in the WebContent/resources/style directory.

The Internationalization uses I18Next Framework. The JSON File for Internationalization is located in the WebContent/resources/locales/lang/translation file.

Please feel free to test this code, submit issues, and help make Self-Certification
around the OpenChain Specification easier for everyone.


# Setting Up Development Environment
The OpenChainCertification run on the AWS Elastic Beanstalk runtime environment.
The original application was developed using Eclipse with the AWS SDK for Java plugins.
The following outline the steps used to create an Eclipse based development environment.
1. Install or Update Eclipse for Java EE: http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/keplersr2
2. Install the AWS Toolkit for Eclipse: https://aws.amazon.com/eclipse/
3. Clone this project
4. Create a new Eclipse Faceted project
4.1 Change the default location to the location of the cloned project
4.2 Select the Dynamic Web Module, Java 1.7 and JavaScript facets
5. Right click on the project and select build path/add libraries
5.1 Add the AWS SDK for Java as a library
5.2 Add the Server library/AWS Elastic Beanstalk J2EE Runtime library
5.2 Add Junit / JUnit 4 library
5.3 Add all of the jar files from WebContent/WEB-INF/lib to the build path
6. Right click on the test folder and add as a source directory to the build path
7. Configure a PostgreSQL server on local host and create a database with the configuration specified in the org.openchain.certification.TestHelper.java file.
8. You should be able to run the unit tests successfully
7. At this point, there should be no compiler errors and you should be able to successfully run the unit tests
To run the actual application locally, create a Tomcat 7 server in eclipse and run the application under the Tomcat server
You may need to create a database or modify some of the database parameters.
