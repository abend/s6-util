#s6-util

## Synopsis

Some basic classes that can be used in multiple applications. It may appear to be a somewhat strange assortment of odds-and-ends, but much of that is used in s6-db and is therefore dependent on them.

## Code Example

Coming Soon

## Installation

Include the following in your maven project

    <dependency>
       <groupId>com.samsix</groupId>
       <artifactId>s6-util</artifactId>
       <version>1.0.0</version>
    </dependency>

or clone and build yourself using

    mvn clean install

## API Reference

You can create a source jar

    mvn source:jar

## Tests

Sadly, no tests at present as I never moved them out of the project they were in to this Open Source Project.

##Publishing

###Snapshot

Snapshot deployment are performed when your version ends in -SNAPSHOT. You do not need to fulfil the requirements when performing snapshot deployments and can simply run

    mvn clean deploy
    
Successfully deployed SNAPSHOT versions will be found in Snapshot repository. If you need to delete your artifacts, you can log in to [Sonatype’s Nexus](https://oss.sonatype.org) using the same credentials you use to access to the Sonatype JIRA.

###Release

The Maven Release Plugin can be used to automate the changes to the Maven POM files, sanity checks, the SCM operations required, the actual deployment execution and you can perform a release deployment to OSSRH with the following steps.

Prepare the release by answering the prompts for versions and tags

    mvn release:clean release:prepare
    
If prepare fails use

    mvn release:rollback
    mvn release:clean
    
The prepare will create a new tag in SCM, even in GitHub, automatically checking in on your behalf. For it to work you need to have working public key in GitHub for git-push.

Perform the release.

    mvn release:perform

The perform process will ask for your gpg.passphrase if you don’t give it with passphrase argument.

Or you can, as I do, set it in your ~/.m2/settings.xml file.

    <settings>
      <servers>
        <server>
          <id>ossrh</id>
          <username>crowmagnumb</username>
          <password>PASSWORD</password>
        </server>
      </servers>
    </settings>

You need your public key on one of the public sites. I used [mit](http://pgp.mit.edu:11371/). I obtained my public key to paste into the site via ...

    gpg --output ~/public.gpg --armor --export crowmagnumb@gmail.com
    cat ~/public.gpg
    
This execution will deploy to OSSRH and release to the Central Repository in one go, thanks to the usage of the Nexus Staging Maven Plugin with autoReleaseAfterClose set to true.

Now your artifacts are in the Releases repository. The updates to The Central Repository search can take up to two hours. Once your artifacts are released you will not be allowed to update or delete them.

The first time you promote a release, you need to comment on the OSSRH JIRA ticket you created so OSSRH can know you are ready to be synced.


