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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a JSON with the login-status of the user:
  * If the user is logged-in, the JSON object contains user's email and a logout link.
  * Otherwise the JSON object contains a login link.
  * In both cases, the JSON contains "isLoggedIn", a boolean used to check login status.
 */
@WebServlet("/login-status")
public class LoginStatusServlet extends HttpServlet {
  private static UserService userService;
  private static String loginUrl;
  private static String logoutUrl;

  @Override
  public void init() {
    userService = UserServiceFactory.getUserService();
    // create a logout url and return to main page after the user has logged out
    logoutUrl = userService.createLogoutURL("/");
    // create a login url and return to main page after the user has logged in
    loginUrl = userService.createLoginURL("/");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    JsonObject json = new JsonObject();

    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      
      json.addProperty("isLoggedIn", true);
      json.addProperty("email", userEmail);
      json.addProperty("logoutLink", logoutUrl);

    } else {
      json.addProperty("isLoggedIn", false);
      json.addProperty("loginLink", loginUrl);
    }

    response.getWriter().println(json.toString());
  }
}
