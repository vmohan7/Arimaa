Arimaa
======

CS 229 Project

How to run the starter code:
- Clone / pull latest code
- Open Eclipse and select File -> New -> Java Project
- Uncheck "Use Default Location" and browse for the "ArimaaBot" directory in this git repo
- Right click on the directory in the project browser -> Run as... -> Run Configurations
- Click the "Arguments" tab, enter these VM arguments, and hit apply: -Xmx500000000
- Run ArimaaEngine.java

Migrating Data:
- Get zip from Google Drive
- Unzip
- Copy the following lines of code with ConvertData as the pwd:

<pre><code>
mysqladmin create Arimaa           # create database; may need to use 'sudo'
cat FastSQLLoad/*.sql | mysql -u root Arimaa   # create tables in database
mysqlimport --local -u root Arimaa FastSQLLoad/*.txt   # load data into tables; may need to specify full path
</code></pre>

Other items: 
- Use MySQL5.5
