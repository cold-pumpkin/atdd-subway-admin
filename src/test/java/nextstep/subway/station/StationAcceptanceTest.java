package nextstep.subway.station;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends BaseStationAcceptanceTest {
    @BeforeEach
    void init() {
        super.setUp();
    }

    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        ExtractableResponse<Response> response = createStationRequest("강남역");

        // then
        assertThat(response.statusCode())
                .isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> stationNames = findStationsRequest().jsonPath().getList("name", String.class);
        assertThat(stationNames).containsAnyOf("강남역");
    }

    /**
     * Given 지하철역을 생성하고
     * When 기존에 존재하는 지하철역 이름으로 지하철역을 생성하면
     * Then 지하철역 생성이 안된다
     */
    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
    @Test
    void createStationWithDuplicateName() {
        // given
        createStationRequest("강남역");

        // when
        ExtractableResponse<Response> response = createStationRequest("강남역");

        // then
        assertThat(response.statusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("지하철역을 조회한다.")
    @Test
    void getStations() {
        // given
        createStationRequest("강남역");
        createStationRequest("선릉역");

        // when
        List<String> stationNames = findStationsRequest().jsonPath().getList("name", String.class);

        // then
        assertThat(stationNames)
                .hasSize(2)
                .containsExactlyInAnyOrder("강남역", "선릉역");
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStation() {
        // given
        int stationId = createStationRequest("강남역").jsonPath().get("id");

        // when
        deleteStationRequest(stationId);

        // then
        List<String> stationNames = findStationsRequest().jsonPath().getList("name");
        assertThat(stationNames)
                .isEmpty();
    }
}
