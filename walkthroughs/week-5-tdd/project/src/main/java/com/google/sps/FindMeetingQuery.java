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

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class FindMeetingQuery {

  /** Returns a Collection of time ranges when meeting {@code request} can be scheduled in the day of 
    * events so that all attendees are free. 
    */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      // if the meeting lasts more than a day, there is no solution
      return new ArrayList<TimeRange>();
    }

    // array that will represent the number of meetings happening during that minute
    ArrayList<Integer> meetings = new ArrayList<Integer>();
    // before processing the events, the number of meetings for each minute is 0
    meetings.addAll(Collections.nCopies(TimeRange.END_OF_DAY - TimeRange.START_OF_DAY + 1, 0));
    Collection<String> attendees = request.getAttendees();

    // If at least one requested attendee is participating in the event
    // update the number of meetings for the start and end time of event
    events.parallelStream().filter(event -> (event.containsRequestedAttendees(attendees) == true))
                            .map(Event::getWhen)
                            .collect(Collectors.toList())
                            .forEach(eventWhen -> updateNumberOfMeetings(meetings, eventWhen));

    // after all events are processed, for each minute x, meetings[x] will represent the number of
    // meetings that start at minute x, minus the number of meetings that end right before minute x

    // precompute the prefix sum on the meetings array s.t meetings[x] will represent the number of
    // meetings that happen at minute x:
    // (before computing the sum):  [ .... +1 ........ -1 .... ]
    // (after computing the sum) :  [..... +1 +1 ... +1 0 .... ]
    precomputePrefixSum(meetings);

    return findAvailableTimeRanges(meetings, (int)request.getDuration());
  }

  /** In meetings array, add 1 to the start time of the meeting and substract 1 from the end time
    * Only the endpoints are changed such that after all events are processed and the prefix sum is
    * computed, the number of meetings increases in the array starting from start time and ending
    * right before the end time.
   */
  private void updateNumberOfMeetings(ArrayList<Integer> meetings, TimeRange when) {
    // mark that a new meeting starts at when.start()
    meetings.set(when.start(), meetings.get(when.start()) + 1); 
    // mark that a new meeting ends right before when.end() 
    if (when.end() <= TimeRange.END_OF_DAY) {
      // only update the end time of events that end before the day ends
      meetings.set(when.end(), meetings.get(when.end()) - 1);
    }
  }
  /** Computes prefix sum on meetings array such that meetings[x] will represent the number of
    * meetings that happen at minute x.
    */
  private void precomputePrefixSum(ArrayList<Integer> meetings) {
      // compute the sum starting from the second element because the prefix sum for the first 
      // element is the first element 
      for (int i = TimeRange.START_OF_DAY + 1; i <= TimeRange.END_OF_DAY; ++ i) {
        meetings.set(i, meetings.get(i - 1) + meetings.get(i));
      }
  }

  /** Given meetings = an array where each element represents the number of meeting that occur at that
   * minute, and a duration, returns a Collection of time ranges in which the meeting lasting duration
   * minutes can happen. 
  */
  private Collection<TimeRange> findAvailableTimeRanges(ArrayList<Integer> meetings, int duration) {
    ArrayList<TimeRange> availableTimeRange = new ArrayList<TimeRange>();

    int lastUnavailableTime = TimeRange.START_OF_DAY - 1;
    
    for (int endingTime = TimeRange.START_OF_DAY; endingTime <= TimeRange.END_OF_DAY; ++ endingTime) {
      if (meetings.get(endingTime) != 0) {
        // at least one meeting is scheduled during minute endingTime
        lastUnavailableTime = endingTime;
      }
      if (endingTime == TimeRange.END_OF_DAY || meetings.get(endingTime + 1) != 0) {
        // there are no meetings during (lastUnavailableTime, endingTime]
        if (endingTime - lastUnavailableTime >= duration) {
          // add [lastUnavailableTime + 1, endingTime] as an available time range for the meeting
          TimeRange timeRange = TimeRange.fromStartEnd(lastUnavailableTime + 1, endingTime, /* inclusive = */ true);
          availableTimeRange.add(timeRange);
        }
      } 
    }

    return availableTimeRange;
  }
}
