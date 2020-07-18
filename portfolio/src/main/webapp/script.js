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
 * Fetches comments from DataServlet and adds them to the DOM as a list.
 */
function getComments() {
  fetch('/data').then(response => response.json()).then((comments) => {

    const commentsListElement = document.getElementById('comments-history');
    
    commentsListElement.innerHTML = '';
    for (const commentIndex in comments) {
      commentsListElement.appendChild(
          createListElement(comments[commentIndex]));
    }
  });
}
/*
  Returns HTML list element with text 
*/
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
