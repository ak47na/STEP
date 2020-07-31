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

package com.google.sps;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;


public final class FindMeetingQuery {
  // array representing the number of meetings happening during that minute
  private ArrayList<Integer> meetings;

  public FindMeetingQuery() {
    meetings = new ArrayList<Integer>();
    //before processing the events, the number of meetings for each minute is 0
    meetings.addAll(Collections.nCopies(TimeRange.END_OF_DAY + 2, 0));
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request, Collection<String> optionalAttendees) {
    Collection<String> allAttendees = request.getAttendees();
    allAttendees.addAll(optionalAttendees);

    MeetingRequest newRequest = new MeetingRequest(allAttendees, request.getDuration());
    Collection<TimeRange> result = queryNoOptionalAttendees(events, newRequest);
    if (result.size() > 0) {
      return result;
    }
    return queryNoOptionalAttendees(events, request);
  }

  /** returns a Collection of time ranges when meeting {@code request} can be scheduled in the day of 
    * events so that all attendees are free */
  public Collection<TimeRange> queryNoOptionalAttendees(Collection<Event> events, MeetingRequest request) {

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      // if the meeting lasts more than a day, there is no solution
      return new ArrayList<TimeRange>();
    }

    Collection<String> attendees = request.getAttendees();

    for (Event event : events) {
      // at least one requested attendee is participating in the event
      if (event.containsRequestedAttendees(attendees)) {
        // update the number of meetings during the time of event
        updateNumberOfMeetings(event.getWhen());
      }
    }

    return findAvailableTimeRanges((int)request.getDuration());
  }

  private void updateNumberOfMeetings(TimeRange when) {
    // mark that a new meeting starts at when.start()
    meetings.set(when.start(), meetings.get(when.start()) + 1); 
    // mark that a new meeting ends right before when.end() 
    meetings.set(when.end(), meetings.get(when.end()) - 1);
  }

  /** returns a Collection of time ranges in which the meeting lasting duration minutes can happen */
  private Collection<TimeRange> findAvailableTimeRanges(int duration) {
    ArrayList<TimeRange> availableTimeRange = new ArrayList<TimeRange>();

    int lastUnavailableTime = TimeRange.START_OF_DAY - 1;
    
    for (int endingTime = TimeRange.START_OF_DAY; endingTime <= TimeRange.END_OF_DAY; ++ endingTime) {
      meetings.set(endingTime + 1, meetings.get(endingTime) + meetings.get(endingTime + 1));

      if (meetings.get(endingTime) != 0) {
        // at least one meeting is scheduled during minute endingTime
        lastUnavailableTime = endingTime;
      }
      if (meetings.get(endingTime + 1) != 0 || endingTime == TimeRange.END_OF_DAY) {
        // there are no meetings during (lastUnavailableTime, endingTime]
        if (endingTime - lastUnavailableTime >= duration) {
          // add [lastUnavailableTime + 1, endingTime] as an available time range for the meeting
          TimeRange timeRange = TimeRange.fromStartEnd(lastUnavailableTime + 1, endingTime, true);
          availableTimeRange.add(timeRange);
        }
      } 
    }

    return availableTimeRange;
  }
}