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

/**
 * Displays a random quote to the page.
 */
function showRandomQuote() {
  const quotes =
      ['One day, one lifetime', 
       'The moment you say “I know everything” is the end of your growth',
       'Shoot for the moon. Even if you miss it you will land among the stars'];

  // Pick a random quote.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('random-quote-container');
  quoteContainer.innerText = quote;
}

/**
 * Generates a URL for a random image in the images directory and adds an img
 * element with that URL to the page.
 */
function randomizeImage() {
  // The images directory contains 2 images, so generate a random index between
  // 1 and 2.
  const imageIndex = Math.floor(Math.random() * 2) + 1;
  const imgUrl = 'images/img' + imageIndex + '.jpeg';

  const imgElement = document.createElement('img');
  imgElement.src = imgUrl;

  const imageContainer = document.getElementById('random-image-container');
  // Remove the previous image.
  imageContainer.innerHTML = '';
  imageContainer.appendChild(imgElement);
}

/**
 * Fetches a random name from the server and adds it to the DOM.
 */
function getRandomName() {
  fetch('/data').then(response => response.text()).then((name) => {
    document.getElementById('name-container').innerText = name;
  });
}

/**
 * Fetches the last commentsLimit comments from DataServlet and adds them to the DOM as a list.
 * commentsLimit is selected by the user and sent to the server as parameter in the query string.
 */
function getComments() {
  const dataURL = `/data?commentsLimit=${document.getElementById('commentsLimit').value}`;

  fetch(dataURL).then(response => {
      if (response.status == 200) {
        return response.json();
      } else {
        if (response.status == 400) {
          throw new Error("Invalid data: The number of comments should be between 0 and 100");
        } else {
          throw new Error("Error!");
        }
      }
      }).then(comments => {
      const commentsListElement = document.getElementById('comments-history');
    
      commentsListElement.innerHTML = '';

      for (const commentIndex in comments) {
        messageAndScore = `${comments[commentIndex].message}(${comments[commentIndex].score})`;
        // display the message, the sentiment score and the nickname/email of the user in format:
        // message(sentiment score) : nickname 
        // TODO[ak47na]: display the sentiment score in a more descriptive way

        commentItem = createListElement(`${messageAndScore}: ${comments[commentIndex].userData}`);
        imageElement = createImageElement(comments[commentIndex].imageBlobstoreKey);
        // if the comment contains an image add it to the message
        if (imageElement !== null) 
          commentItem.appendChild(imageElement);
        
        commentsListElement.appendChild(commentItem);
      }
      }).catch(dataError => {
      alert(dataError);
    });
}

/** 
 * Sends a POST request to DeleteDataServlet to delete all the comments and calls getComments() to
 * remove the comments from the page.
 */
function deleteAllComments() {
  fetch('delete-data', {method: 'POST'}).then(getComments());
}

/** 
 * Returns HTML list element with text. 
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/** 
 * Returns HTML image element from url.
 */
function createImageElement(imageBlobstoreKey) {
  if (!imageBlobstoreKey) 
    return null;
  const imgElement = document.createElement('img');
  // imgElement is stored in blobstore with the key imageBlobstoreKey
  // To get the image,the browser sends a GET request to BlobServlet
  imgElement.src = `/retrieve-blobstore?blob=${imageBlobstoreKey}`;
  return imgElement;
}

function updateVisibilityForLoginStatus() {
  displayElement('commentForm', false);
  displayElement('loginLink', false);
  displayElement('logoutLink', false);
  displayElement('changeNicknameLink', false);

  fetch('/login-status').then(response => response.json()).then(loginStatus => {

    // Get the login status for the user and only show the appropriate bits
    if (loginStatus.isLoggedIn === true) {
      // the user is logged in, then unhide commentForm and the logout url
      displayElement('commentForm', true);
      //when commentForm is displayed, its action should be updated to the Blobstore URL
      fetchBlobstoreUrlAndSetFormAction('commentForm');
      updateLink('logoutLink', loginStatus.logoutLink);
      displayElement('logoutLink', true);
      displayElement('changeNicknameLink', true);
    } else {
      // unhide login url
      updateLink('loginLink', loginStatus.loginLink);
      displayElement('loginLink', true);
    }
  });
}

/**
 * Fetch BlobUploadUrl from BlobstoreUploadServlet and set it as the action for the comment form
 * When the form is submitted, the request goes to Blobstore which handles the image upload.
 * Then, Blobstore forwards the request to DataServlet.
 */
function fetchBlobstoreUrlAndSetFormAction(commentFormId) {
  fetch('/blobstore-upload-url').then(response => response.text()).then(BlobUploadUrl => {
    const commentForm = document.getElementById(commentFormId);
    commentForm.action = BlobUploadUrl;
  });
}

/**
 * Display the element with id=elementId if isShown === true, otherwise hide it.
 */
function displayElement(elementId, isShown) {
  element = document.getElementById(elementId);
  if (isShown === true) {
    element.style.display = 'block';
  } else {
    element.style.display = 'none'; 
  }
}

/**
 * Update the element with id=elementId to link to link.
 */
function updateLink(elementId, link) {
  element = document.getElementById(elementId);
  element.setAttribute("href", link);
}
