package app.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO {

    @JsonProperty("title")
    private String title;
    @JsonProperty("author_name")
    private List<String> author;
    @JsonProperty("first_publish_year")
    private int publish_year;

}
