<!--
  ~ Copyright (c) 2014 Wipro Limited
  ~ All Rights Reserved
  ~
  ~ This code is protected by copyright and distributed under
  ~ licenses restricting copying, distribution and decompilation.
  -->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security"
	   uri="http://www.springframework.org/security/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>


    		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    		<script src="../js/jquery.min.js"></script>
    		<link href="../css/jquery-ui-1.10.3.custom.css" rel="stylesheet">
    		<link href="../css/css/bootstrap.min.css" rel="stylesheet" />
    		<script src="../js/jquery-ui-1.10.3.custom.js"></script>
    		<script src="../js/jquery.steps.min.js"></script>
    		<link rel="stylesheet" href="../css/jquery.steps.css" />
    		<link rel="stylesheet" href="../css/jquery.steps.custom.css" />
    		<link href="../css/bootstrap.custom.css" rel="stylesheet" type="text/css" />
    		<script src="../js/bootstrap.js" type="text/javascript"></script>
            <script src = "../js/jquery.fancytree.js" ></script >
            <link rel = "stylesheet" href = "../css/ui.fancytree.css" />
            <script src = "../js/jquery.fancytree.gridnav.js" type = "text/javascript" ></script >
            <script src = "../js/jquery.fancytree.table.js" type = "text/javascript" ></script >
    		<script src="../js/jquery.jtable.js" type="text/javascript"></script>
    		<script src="../js/angular.min.js" type="text/javascript"></script>
    		<link href="../css/jtables-bdre.css" rel="stylesheet" type="text/css" />

	<script>
    	  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    	  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    	  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    	  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
    	  //Please replace with your own analytics id
    	  ga('create', 'UA-72345517-1', 'auto');
    	  ga('send', 'pageview');
    	</script>


        <script>

                 var app = angular.module('myApp', []);
                  app.controller('myCtrl', function($scope) {
                      $scope.processIds = {};
                      $.ajax({
                      url: '/mdrest/tdfilehandler/teradata/',
                          type: 'POST',
                          dataType: 'json',
                          async: false,
                          success: function (data) {
                              $scope.processIds = data;
                          },
                          error: function () {
                              alert('danger');
                          }
                      });
                  });
          </script>

         <script type="text/javascript">
                function uploadFile(processId) {
                    var processIdValue = $('#'+processId).val();
                    var fd = new FormData();
                    var fileObj = jQuery('#file')[0].files[0];
                    var fileName=fileObj.name;
                    fd.append("file", fileObj);
                    fd.append("name", fileName);
                    $.ajax({
                      url: "/mdrest/tdfilehandler/uploadtr/"+processIdValue,
                      type: "POST",
                      data: fd,
                      enctype: 'multipart/form-data',
                      processData: false,  // tell jQuery not to process the data
                      contentType: false   // tell jQuery not to set contentType
                    }).done(function( data ) {

                        console.log( data );
                        alert('Successfully uploaded');
                    });
                    return false;
                }
            </script>

    </head>

    <body ng-app="myApp" ng-controller="myCtrl">
        <div class="form-group">
           <label class="control-label col-sm-2" for="processId">Process ID</label>
           <div class="col-sm-10">
               <select class="form-control" id="processId" name="processId" >
                       <option ng-repeat="processId in processIds.Options" value="{{processId.Value}}" name="processId">{{processId.DisplayText}}</option>

               </select>
           </div>
       </div>

       <div class="col-sm-2">
       <label>Upload file</label><br>
            <input type="file" name="file" class="form-control" id="file" required>
            <div ><br /></div >
                <input type="button" onClick="uploadFile('processId')" value="Upload" class="btn btn-primary"/>

            </div >
    </body>
</html>