/*
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
*/

define(function() {
    "use strict";

    var HTTPCSVExtensionDialogController = function(acquisitionState, oDeferred, fServiceCall, workflow) {

        /*
        Create dialog controls
        */
        var dLayout = new sap.ui.commons.layout.MatrixLayout({
            layoutFixed : true,
            columns : 2,
            width : "570px",
            widths : [ "20%", "80%" ]
        });
        
        // Dataset Name
        var datasetNameTxt = new sap.ui.commons.TextField({
            width : '100%',
            value : "",
            enabled : workflow === "CREATE"
        });

        var datasetNameLbl = new sap.ui.commons.Label({
            text : "Dataset Name:",
            labelFor : datasetNameTxt
        });

        dLayout.createRow({
            height : "30px"
        }, datasetNameLbl, datasetNameTxt);
        
        //URL
        var urlTxt = new sap.ui.commons.TextField({
            width : '100%',
            value : 'http://'
        });

        var urlLbl = new sap.ui.commons.Label({
            text : "URL:",
            labelFor : urlTxt
        });

        dLayout.createRow({
            height : "30px"
        }, urlLbl, urlTxt);
        
        //Request Type
        var requestTypeDropdown = new sap.ui.commons.DropdownBox("RequestTypeDropdown");
        requestTypeDropdown.setTooltip("Request Type");
        requestTypeDropdown.setEditable(true);
        requestTypeDropdown.setWidth("20%");
        
        var requestItem = new sap.ui.core.ListItem("Get");
        requestItem.setText("GET");
        requestTypeDropdown.addItem(requestItem);
        
        requestItem = new sap.ui.core.ListItem("Post");
        requestItem.setText("POST");
        requestTypeDropdown.addItem(requestItem);
        
        requestItem = new sap.ui.core.ListItem("Put");
        requestItem.setText("PUT");
        requestTypeDropdown.addItem(requestItem);
        

        var requestTypeLbl = new sap.ui.commons.Label({
            text : "Request Type:",
            labelFor : requestTypeDropdown
        });

        dLayout.createRow({
            height : "30px"
        }, requestTypeLbl, requestTypeDropdown);
        
        //Username
        var usernameTxt = new sap.ui.commons.TextField({
            width : '100%',
            value : ""
        });

        var usernameLbl = new sap.ui.commons.Label({
            text : "Username:",
            labelFor : usernameTxt
        });

        dLayout.createRow({
            height : "30px"
        }, usernameLbl, usernameTxt);
        
        //Password
        var passwordTxt = new sap.ui.commons.PasswordField({
            width : '100%',
            value : ""
        });

        var passwordLbl = new sap.ui.commons.Label({
            text : "Password:",
            labelFor : passwordTxt
        });

        dLayout.createRow({
            height : "30px"
        }, passwordLbl, passwordTxt);
        
        //Body
        var bodyTxt = new sap.ui.commons.TextArea({
            width : '100%',
            value : ""
        });
        
        bodyTxt.setRows(5);

        var bodyLbl = new sap.ui.commons.Label({
            text : "Body:",
            labelFor : bodyTxt
        });

        dLayout.createRow({
            height : "130px"
        }, bodyLbl, bodyTxt);
        
         /*
        Button press events
        */
        var buttonCancelPressed = function() {
        	oDeferred.reject(); // promise fail
            dialog.close(); // dialog is hoisted from below
        };
                
        var buttonOKPressed = function() {
            var info = {};
            
            info.url = urlTxt.getValue();
            info.requesttype = requestTypeDropdown.getValue();	
            info.username = usernameTxt.getValue();
            info.password = passwordTxt.getValue();
            info.body = bodyTxt.getValue();
            
            info.datasetName =  datasetNameTxt.getValue();
            
            acquisitionState.info = JSON.stringify(info);
            oDeferred.resolve(acquisitionState, datasetNameTxt.getValue());
            dialog.close();
        };

        var okButton = new sap.ui.commons.Button({
            press : [ buttonOKPressed, this ],
            text : "Send",
            tooltip : "Send"
        }).setStyle(sap.ui.commons.ButtonStyle.Accept);

        var cancelButton = new sap.ui.commons.Button({
            press : [ buttonCancelPressed, this ],
            text : "Cancel",
            tooltip : "Cancel"
        }).addStyleClass(sap.ui.commons.ButtonStyle.Default);

        var onClosed = function() {
        	this.destroy();
            oDeferred.reject();
        };
        
        /*
        Modify controls based on acquisitionState
        */
        var envProperties = acquisitionState.envProps;
        if (acquisitionState.info) {
            var info = JSON.parse(acquisitionState.info);
            
            urlTxt.setValue(info.url);
            requestTypeDropdown.setValue(info.requesttype);
            usernameTxt.setValue(info.username);
            passwordTxt.setValue(info.password);
            bodyTxt.setValue(info.body);
            
            envProperties.datasetName = info.datasetName;
        }
        datasetNameTxt.setValue(envProperties.datasetName);

        /*
        Create the dialog
        */
        var dialog = new sap.ui.commons.Dialog({
            width : "720px",
            height : "480px",
            modal : true,
            resizable : false,
            closed : onClosed,
            content: [dLayout],
            buttons : [okButton, cancelButton]
        });
        dialog.setTitle("HTTP CSV Extension: " + envProperties.datasetName);

        this.showDialog = function() {
            dialog.open();
        };
    };

    return HTTPCSVExtensionDialogController;
});