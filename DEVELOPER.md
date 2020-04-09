# Developer Notes

## How to release

This sbt project uses [sbt-git](https://github.com/sbt/sbt-git) to determine the version of this project.
To release a new version of this plugin, follow the instructions below.

### 1. Clean repository

Make sure that you do not have any unstaged changes in your git repository. Run

    $ git status

If you have any unstaged changes, commit or clean them.

### 2. Check the latest version

Run

    $ git tag
    
to see what versions have been released so far and determine the version of the
next release.

### 3. Add tag

Once you determine the version, run the command below to add a new tag.

    $ git tag -a "vx.y.z" -m "New release version x.y.z"

### 4. Test locally

Before publish this plugin, you should test it locally. To install this plugin 
into your local repository, run this command.

    $ sbt pulishLocal
    
This command installs this plugin into `~/.ivy2/local`.

### 5. Create a repository on Bintray

(skip this step if you already have "sbt-plugins" repository on Bintray.)

This project uses [sbt-bintray](https://github.com/sbt/sbt-bintray) plugin to publish
this CcPlugin to [Bintray](https://bintray.com/). If you have not created your account
on Bintray, create a free one.

If you have not created "sbt-plugins" repository on your Bintray account yet, create it
as instructed in 
[Bintray For Plugins](https://www.scala-sbt.org/1.x/docs/Bintray-For-Plugins.html).
When you create the repository, make sure that the name is "sbt-plugins" and type is
"Generic".

### 6. Configure your Bintray credentials

(skip this step if you have already configured your Bintray credentials)

If have not configured your Bintray credentials in your computer, first you need
to get API Key. Follow the steps below to get the API Key.  

* Go to [your Bintray profile page](https://bintray.com/profile/edit).
* Click "API Key" in the left menu.
* If you are asked, re-enter your password of your Bintray account and press "Submit" button.
* Press "Show" link to show your API Key.
    
[sbt-bintray](https://github.com/sbt/sbt-bintray) plugin reads credential information
from `~/.bintray/.credentials`. If you have not created this file, create it with 
the following contents

    realm = Bintray API Realm
    host = api.bintray.com
    user = (your bintray user name)
    password = (your API Key)
    
For password field, provide your API Key, not the password you enter when you login Bintray.

Finally, in this project directory, run
   
    $ sbt bintrayWhoami
    
to see if your Bintray user name is correctly loaded from the credential settings.

### 7. Publish to Bintray

Once the credential settings are completed, run the commands below in this project
directory to publish your new version.

    $ sbt publish
    $ sbt bintrayRelease
    
If these commands are successful, you should be able to see the new version of sbt-cc
in your repository. You can see your new plugin here

     https://bintray.com/(your_account_name)/sbt-plugins/sbt-cc
    
and

     https://dl.bintray.com/(your_account_name)/sbt-plugins/
   
## References

* [Bintray For Plugins](https://www.scala-sbt.org/1.x/docs/Bintray-For-Plugins.html)
* [Publishing an SBT Project onto Bintray: an Example](http://queirozf.com/entries/publishing-an-sbt-project-onto-bintray-an-example)
* [sbt-git-versioning](https://github.com/rallyhealth/sbt-git-versioning): This project does not use this sbt-git-versioning plugin, but its documentation includes useful information about how [sbt-git](https://github.com/sbt/sbt-git) determines the version.
