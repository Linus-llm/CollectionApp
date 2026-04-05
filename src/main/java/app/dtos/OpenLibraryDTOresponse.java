package app.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryDTOresponse {
    @JsonProperty("docs")
    private List<BookDTO> docs;

    public List<BookDTO> getDocs() {
        return docs;
    }
}
