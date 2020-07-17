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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import com.google.gson.Gson;
import java.util.*; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<String> comments = new ArrayList<String>();
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String commentsJson = gson.toJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(commentsJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the comment from the form
    String newComment = getNewComment(request);
    if (newComment == "") {
      response.setContentType("text/html");
      response.getWriter().println("Please enter a valid comment.");
      return;
    }
    
    comments.add(newComment);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private String getNewComment(HttpServletRequest request) {
    String newComment = request.getParameter("new-comment");

    byte[] commentBytes = null;
    try {
      commentBytes = newComment.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      System.err.println("Invalid comment: " + newComment);
      return "";
    }

    return newComment;
  }
}
