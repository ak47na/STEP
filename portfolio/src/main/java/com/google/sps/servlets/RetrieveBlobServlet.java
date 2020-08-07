// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is called to display images stored in Blobstore given their blobKey.
 */
@WebServlet("/retrieve-blobstore")
public class DisplayBlobServlet extends HttpServlet {

  @Override
  /** 
    * Handles GET requests to display the image stored in Blobstore. The serve function arranges
    * for blob with the blobKey specified in the qury string to be served as the response content
    * for the current request. 
    */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    try {
      BlobKey blobKey = new BlobKey(request.getParameter("blob"));
      blobstoreService.serve(blobKey, response);
    } catch (Exception e) {
      // Send a HTTP 400 Bad Request response if the blob parameter is null or 
      // if the blob key was not found in Blobstore
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }
}
