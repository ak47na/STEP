package com.google.sps.data;

import java.util.*; 

public class Comment {
  private static String message;
  private static String userEmail;
  
  public Comment(String commentMessage, String commentUserEmail) {
    this.message = commentMessage;
    this.userEmail = commentUserEmail;
  }
}