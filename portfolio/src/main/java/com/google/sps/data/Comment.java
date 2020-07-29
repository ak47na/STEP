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

package com.google.sps.data;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;

/** Class to store user's website comments. */
public final class Comment {

  private final String message;
  private final String userData;
  private final String imageUrl;
  private final float score;

  public Comment(String message, String userData, String imageUrl) throws IOException {
    this.message = message;
    this.userData = userData;
    this.imageUrl = imageUrl;
    this.score = getMessageScore(message);
  }
  /** 
   * Returns the sentiment score(between -1.0 and 1.0) of the message
   * @throws {IOException} if it fails to establish connection to LanguageService
   */
  private float getMessageScore(String message) throws IOException {
    //instantiate the Language client to apply sentiment analysis on the text of message
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      Document doc = Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      if (sentiment == null)
        // no sentiment was found
        return 0;
      return sentiment.getScore();
    }
  }
}
