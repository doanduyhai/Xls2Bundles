Xls2Properties
================

Presentation
------------------

Xls2Properties is a small Maven plugin to generate resource bundle files with locales from a source XLS file.

The XLS file has:

- A "SETTING" sheet where you indicate the default locale and resource file name to be generated
- A "DEFAULT" sheet where you put all your default resource values
- As many extra sheets as the number of locales you want to support. For example you'll have a "en_US" sheet for American english locale and "fr_FR" for french locale. You can add as many extra sheet you need. The sheet name should respect Java i18n locale naming though (fr or fr_FR for example)

 The plugin will generate as many resource bundles files as there are locale sheets in the XLS, including the DEFAULT locale sheet.
 The DEFAULT sheet will serve as base template for other locales. 
 If a property in "DEFAULT" is not found in a locale sheet, it will be added to this locale (with the message in default language though).
 If a property from "DEFAULT" already exists in the locale sheet, its value will be kept and not overriden
 If a property exists in the local sheet but not in the "DEFAULT" sheet, it will also be kept
 
 Ex: 
 
 <table>
	<thead>
		<tr>
			<th>DEFAULT sheet (en_GB)<th>
			<th>en_US sheet<th>
			<th>fr sheet<th>
			<th>Generated en_GB.properties<th>
			<th>Generated en_US.properties<th>
			<th>Generated fr.properties<th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>
				<ul>
					<li>welcome.message=Welcome to {0}!</li>
					<li>registration.prompt=Please register your account first</li>
					<li>wrong.email.error=The email {0} you provided is not correct</li>
				</ul>
			</td>
			<td>
				<ul>
					<li>spam.warning=You are detected as spammer. Your IP address is logged and transmitted to the FBI</li>
				</ul>			
			</td>
			<td>
				<ul>
					<li>welcome.message=Bienvenue à {0}!</li>
					<li>registration.prompt=Veuillez vous enregistrer avant d'utiliser le service</li>
				</ul>			
			</td>
			<td>
				<ul>
					<li>welcome.message=Welcome to {0}!</li>
					<li>registration.prompt=Please register your account first</li>
					<li>wrong.email.error=The email {0} you provided is not correct</li>
				</ul>			
			</td>
			<td>
				<ul>
					<li>welcome.message=Welcome to {0}!</li>
					<li>registration.prompt=Please register your account first</li>
					<li>wrong.email.error=The email {0} you provided is not correct</li>
					<li>spam.warning=You are detected as spammer. Your IP address is logged and transmitted to the FBI</li>
				</ul>			
			</td>
			<td>
				<ul>
					<li>welcome.message=Bienvenue à {0}!</li>
					<li>registration.prompt=Veuillez vous enregistrer avant d'utiliser le service</li>
					<li>wrong.email.error=The email {0} you provided is not correct</li>
				</ul>			
			</td>			
		</tr>
	</tbody>
 </table>


 Original version of this code was developed by Romain LINSOLAS to manage properties files. I adapt the code for resource bundles

Installation
------------

- Fork the project (or retrieve the zip and put it on your local computer)
- Run "mvn clean install"
- Create a Maven project to generate your resource bundles
- Edit the pom.xml and add the following in the <build><plugins> section (see below)
- Indicate the XLS file you want to read for resource bundle generation in the <xlsFile>
- Indicate also your base directory. By default generated resource bundles will be put at <basedir>/target/resources
- Run mvn "xls2properties:generate" and it's done!

<build>
    <plugins>
      <plugin>
        <groupId>fr.doan</groupId>
        <artifactId>xls2properties-maven-plugin</artifactId>
        <version>1.0</version>
        <configuration>
    		<xlsFile>${project.basedir}/src/main/resources/myXLSFile.xls</xlsFile>
    		<basedir>${project.basedir}</basedir>
  		</configuration>
      </plugin>
    </plugins>
 </build>
  
Note: the plugin works with Excel 2003 format (xls) as well as Excel post 2007 format (xlsx)

License
-------

Copyright 2012 [DuyHai DOAN](http://doanduyhai.wordpress.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this application except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.