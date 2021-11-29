package com.example.restapi.controller.PART_A_B;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;
import com.example.restapi.model.OutputOfSearch;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
public class JSONController {

    private static String PROPERTIES_FILENAME = "youtube.properties";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;
    private static YouTube youtube;

    @ApiOperation(value = "retrieve all video metadata containing the word “telecom” in the title in JSON FORMAT")
    @RequestMapping(value = "/res-in-JSON", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<OutputOfSearch> getResInJSON(@RequestParam(defaultValue = "telecom") String input) {
        Properties properties = new Properties();
        List<OutputOfSearch> result = null;
        try {
            InputStream in = JSONController.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }

        try {
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("youtube-cmdline-search-sample").build();

            if (input.length() < 1) {
                input = "YouTube Developers Live";
            }

            List<String> list = Arrays.asList("id,snippet");
            YouTube.Search.List search = youtube.search().list(list);

            String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(input);

            List<String> type = Arrays.asList("video");
            search.setType(type);

            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse searchResponse = search.execute();

            List<SearchResult> searchResultList = searchResponse.getItems();

            if (searchResultList != null) {
                result = prettyPrint(searchResultList.iterator(), input);
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    private static List<OutputOfSearch> prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        List<OutputOfSearch> outputOfSearchList = new ArrayList<OutputOfSearch>();
        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");
                OutputOfSearch searchResultObj = new OutputOfSearch();
                // searchResultObj.setVideo(rId.getVideoId());
                searchResultObj.setTitle(singleVideo.getSnippet().getTitle());
                searchResultObj.setUrl(thumbnail.getUrl());
                outputOfSearchList.add(searchResultObj);
            }
        }
        return outputOfSearchList;
    }

}
