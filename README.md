![](https://img.shields.io/badge/STATUS-NOT%20CURRENTLY%20MAINTAINED-red.svg?longCache=true&style=flat)

# Important Notice
We have decided to stop the maintenance of this public GitHub repository.

HTTP CSV Extension
==========================================================
SAP Lumira users can now use this connector to import data from HTTP APIs that return CSV files in their responses directly into Lumira documents without having to download and import CSV files manually. This Lumira extension is built with the V2 SAP Lumira Data Access Extension SDK.

Install
-----------------
* Open Extension Manager, `File > Extensions`
* Click `Manual Installation`
* Select the zip file from `\install-extension` in this repo
* Restart SAP Lumira Desktop

Usage
-----------------
* Select `File > New Dataset`
* Select `HTTP CSV Connector` from the list of connectors
* Enter the dataset name and these parameters
 + `URL`: The URL of the API call
 + `Request Type`: Type of the request `GET/POST/PUT`
 + `Username`: Username for Basic Auth
 + `Password`: Password for Basic Auth
 + `Body`: Body of the request
* Select `Send` to import data into a new document

Extras
-------
####extras/apimocker/apimocker.exe
* Utility to serve GET requests locally for testing the extension with a sample CSV file
* Place a CSV file in the `extras/apimocker/public` folder and run `apimocker.exe`
* Send a GET request to `http://localhost:3000/<csv-filename>` in the extension

Build
-----------------
* Please refer to the [Sample Extension project](https://github.com/SAP/lumira-extension-da-sample) for instructions to setup your dev environment, make changes, and build this extension.

Resources
-----------
* SCN Blog post - [Coming soon](https://www.google.com/search?q=baby+cat+pics)

License
---------

    Copyright 2015, SAP SE

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: https://github.com/SAP/lumira-extension-da-httpcsv
