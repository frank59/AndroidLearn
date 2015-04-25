package com.geewaza.android.test001;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;


/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified example based on mp4parser google code open source project.
// http://code.google.com/p/mp4parser/source/browse/trunk/examples/src/main/java/com/googlecode/mp4parser/ShortenExample.java


/**
 * Shortens/Crops a track
 */
public class ShortenExample {

	public static void appendVideo(String to , String[] videos) throws IOException{
        Movie[] inMovies = new Movie[videos.length];
        int index = 0;
        for(String video:videos)
        {
			inMovies[index] = MovieCreator.build(video);
        	index++;
    	}
        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();
        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
			result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (videoTracks.size() > 0) {
			result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }
        Container out = new DefaultMp4Builder().build(result);
        FileChannel fc = new RandomAccessFile(String.format(to), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
    }

	
	public static void main(String[] args) throws IOException {
		String mp3 = "D:/opt/pai/huabanxie.mp3";
		String mp4 = "D:/opt/pai/20150426003341.mp4";
		
		Movie mp3Movie = MovieCreator.build(mp3);
		Movie mp4Movie = MovieCreator.build(mp4);
		
		System.out.println(mp3Movie.getTracks());
	}
}
