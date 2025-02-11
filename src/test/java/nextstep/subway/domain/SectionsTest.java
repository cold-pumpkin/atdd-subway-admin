package nextstep.subway.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SectionsTest {
    Sections 모든구간;
    Section 정자역_강남역;
    Line 신분당선;

    @BeforeEach
    void setUp() {
        Station 정자역 = Station.from("정자역");
        Station 강남역 = Station.from("강남역");

        정자역_강남역 = Section.of(정자역, 강남역, 신분당선, 10000);
        신분당선 = Line.of("신분당선", "bg-red-600", 정자역, 강남역, 10000);

        모든구간 = 신분당선.getSections();
    }

    @Test
    @DisplayName("빈 노선에 새로운 구간을 등록한다.")
    void addFirstSection() {
        assertThat(모든구간.isEmpty())
                .isFalse();
    }

    @Test
    @DisplayName("새로운 상행종점 구간을 등록한다.")
    void addUpFinalSection() {
        Section 광교역_정자역 = Section.of(Station.from("광교역"), Station.from("정자역"), 신분당선, 5000);

        모든구간.addSection(광교역_정자역);

        assertThat(신분당선.getUpFinalSection()).satisfies(상행종점구간 -> {
            Station 상행역 = 상행종점구간.getUpStation();
            Station 하행역 = 상행종점구간.getDownStation();
            assertThat(상행역.getName())
                    .isEqualTo("광교역");
            assertThat(하행역.getName())
                    .isEqualTo("정자역");
        });

    }

    @Test
    @DisplayName("새로운 하행종점 구간을 등록한다.")
    void addDownFinalSection() {
        Section 강남역_논현역 = Section.of(Station.from("강남역"), Station.from("논현역"), 신분당선, 5000);

        모든구간.addSection(강남역_논현역);

        assertThat(모든구간.size())
                .isEqualTo(2);
        assertThat(신분당선.getDownFinalSection()).satisfies(하행종점구간 -> {
            Station 상행역 = 하행종점구간.getUpStation();
            Station 하행역 = 하행종점구간.getDownStation();
            assertThat(상행역.getName())
                    .isEqualTo("강남역");
            assertThat(하행역.getName())
                    .isEqualTo("논현역");
        });
    }

    @Test
    @DisplayName("중간에 새로운 구간을 등록한다.")
    void addMiddleSection() {
        Section 강남역_신사역 = Section.of(Station.from("강남역"), Station.from("신사역"), 신분당선, 5000);
        Section 강남역_논현역 = Section.of(Station.from("강남역"), Station.from("논현역"), 신분당선, 2000);

        모든구간.addSection(강남역_신사역);
        모든구간.addSection(강남역_논현역);

        assertThat(모든구간.size())
                .isEqualTo(3);
        assertThat(신분당선.getUpFinalSection()).satisfies(상행종점구간 -> {
            Station 상행역 = 상행종점구간.getUpStation();
            Station 하행역 = 상행종점구간.getDownStation();
            assertThat(상행역.getName())
                    .isEqualTo("정자역");
            assertThat(하행역.getName())
                    .isEqualTo("강남역");
        });
        assertThat(신분당선.getDownFinalSection()).satisfies(하행종점구간 -> {
            Station 상행역 = 하행종점구간.getUpStation();
            Station 하행역 = 하행종점구간.getDownStation();
            assertThat(상행역.getName())
                    .isEqualTo("논현역");
            assertThat(하행역.getName())
                    .isEqualTo("신사역");
        });
    }

    @Test
    @DisplayName("중복구간을 입력하면 예외를 발생시킨다.")
    void addDuplicateSection() {
        Section 정자역_강남역 = Section.of(Station.from("정자역"), Station.from("강남역"), 10000);

        assertThatThrownBy(() -> 모든구간.addSection(정자역_강남역))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가할 구간은 모두 이미 노선에 등록되어 있습니다.");
    }

    @Test
    @DisplayName("상하행역이 모두 기존 구간에 없으면 예외를 발생시킨다.")
    void addNotExistingUpDownStation() {
        Section 논현혁_신사역 = Section.of(Station.from("논현역"), Station.from("신사역"), 5000);

        assertThatThrownBy(() -> 모든구간.addSection(논현혁_신사역))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가할 구간의 상행역과 하행역 중 하나 이상은 노선에 포함되어 있어야 합니다.");
    }

    @Test
    @DisplayName("추가할 구간이 기존 기존 역 사이보다 길면 예외를 발생시킨다.")
    void tooLongDistance() {
        Section 강남역_신사역 = Section.of(Station.from("강남역"), Station.from("신사역"), 신분당선, 5000);
        Section 강남역_논현역 = Section.of(Station.from("강남역"), Station.from("논현역"), 신분당선, 5500);

        모든구간.addSection(강남역_신사역);

        assertThatThrownBy(() -> 모든구간.addSection(강남역_논현역))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 구간의 길이는 기존 역 사이 길이보다 작아야 합니다.");
    }

    @Test
    @DisplayName("추가할 구간의 위치를 찾을 수 없으면 예외를 발생시킨다.")
    void cannotFindSectionToAdd() {
        Section 광교역_강남역 = Section.of(Station.from("광교역"), Station.from("강남역"), 신분당선, 5000);

        assertThatThrownBy(() -> 모든구간.addSection(광교역_강남역))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("새로운 구간을 추가할 위치를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("등록된 구간들을 실제 순서로 정렬하여 조회한다.")
    void getSectionsInOrder() {
        Section 광교역_정자역 = Section.of(Station.from("광교역"), Station.from("정자역"), 신분당선, 5000);
        Section 강남역_신사역 = Section.of(Station.from("강남역"), Station.from("신사역"), 신분당선, 2000);
        Section 강남역_논현역 = Section.of(Station.from("강남역"), Station.from("논현역"), 신분당선, 1000);

        모든구간.addSection(광교역_정자역);
        모든구간.addSection(강남역_신사역);
        모든구간.addSection(강남역_논현역);

        List<Section> 정렬된_구간 = 모든구간.getSectionsInOrder();

        assertThat(정렬된_구간)
                .hasSize(4);
        assertThat(정렬된_구간).satisfies(구간 -> {
            Section 첫번째_구간 = 구간.get(0);
            Section 두번째_구간 = 구간.get(1);
            Section 세번째_구간 = 구간.get(2);
            Section 네번째_구간 = 구간.get(3);
            assertThat(첫번째_구간.getUpStation().getName())
                    .isEqualTo("광교역");
            assertThat(두번째_구간.getUpStation().getName())
                    .isEqualTo("정자역");
            assertThat(세번째_구간.getUpStation().getName())
                    .isEqualTo("강남역");
            assertThat(네번째_구간.getUpStation().getName())
                    .isEqualTo("논현역");
            assertThat(네번째_구간.getDownStation().getName())
                    .isEqualTo("신사역");
        });
    }

    @Test
    @DisplayName("입력받은 지하철역이 포함된 구간을 제거한다.")
    void removeSectionByStation() {
        Station 정자역 = 정자역_강남역.getUpStation();
        Station 강남역 = 정자역_강남역.getDownStation();
        Station 논현역 = Station.from("논현역");
        Station 신사역 = Station.from("신사역");
        Section 강남역_논현역 = Section.of(강남역, 논현역, 신분당선, 5000);
        Section 논현역_신사역 = Section.of(논현역, 신사역, 신분당선, 7000);
        모든구간.addSection(강남역_논현역);
        모든구간.addSection(논현역_신사역);

        모든구간.removeSection(강남역);

        Section 정자역_논현역 = Section.of(정자역, 논현역, 신분당선, 1500);
        assertThat(모든구간.getSectionsInOrder())
                .hasSize(2)
                .containsExactly(정자역_논현역, 논현역_신사역)
                .satisfies(구간 -> {
                    assertThat(구간.get(0).getDistance())
                            .isEqualTo(정자역_강남역.getDistance() + 강남역_논현역.getDistance());
                    assertThat(구간.get(1).getDistance())
                            .isEqualTo(논현역_신사역.getDistance());
                });
    }

    @Test
    @DisplayName("입력받은 상행종점역이 포함된 구간을 제거한다.")
    void removeUpFinalSection() {
        Station 정자역 = 정자역_강남역.getUpStation();
        Station 강남역 = 정자역_강남역.getDownStation();
        Station 논현역 = Station.from("논현역");
        Section 강남역_논현역 = Section.of(강남역, 논현역, 신분당선, 5000);
        모든구간.addSection(강남역_논현역);

        모든구간.removeSection(정자역);

        assertThat(모든구간.getSectionsInOrder())
                .hasSize(1)
                .containsExactly(강남역_논현역);
    }

    @Test
    @DisplayName("입력받은 하행종점역이 포함된 구간을 제거한다.")
    void removeDownFinalSection() {
        Station 강남역 = 정자역_강남역.getDownStation();
        Station 논현역 = Station.from("논현역");
        Section 강남역_논현역 = Section.of(강남역, 논현역, 신분당선, 5000);
        모든구간.addSection(강남역_논현역);

        모든구간.removeSection(논현역);

        assertThat(모든구간.getSectionsInOrder())
                .hasSize(1)
                .containsExactly(정자역_강남역);
    }

    @Test
    @DisplayName("마지막 구간을 제거하려고 하면 예외를 발생시킨다.")
    void removeLastSection() {
        Station 강남역 = 정자역_강남역.getDownStation();

        assertThatThrownBy(() -> 모든구간.removeSection(강남역))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 지하철 구간이 하나인 경우 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 구간을 제거하려고 하면 예외를 발생시킨다.")
    void removeNonExistingSection() {
        Section 강남역_논현역 = Section.of(Station.from("강남역"), Station.from("논현역"), 신분당선, 5000);
        모든구간.addSection(강남역_논현역);

        assertThatThrownBy(() -> 모든구간.removeSection(Station.from("신사역")))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 구간은 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("등록된 역들을 실제 순서로 정렬하여 조회한다.")
    void getStationsInOrder() {
        Station 광교역 = Station.from("광교역");
        Station 정자역 = 정자역_강남역.getUpStation();
        Station 강남역 = 정자역_강남역.getDownStation();
        Station 논현역 = Station.from("논현역");
        Station 신사역 = Station.from("신사역");
        Section 광교역_정자역 = Section.of(광교역, 정자역, 신분당선, 5000);
        Section 강남역_신사역 = Section.of(강남역, 신사역, 신분당선, 2000);
        Section 강남역_논현역 = Section.of(강남역, 논현역, 신분당선, 1000);

        모든구간.addSection(광교역_정자역);
        모든구간.addSection(강남역_신사역);
        모든구간.addSection(강남역_논현역);

        assertThat(모든구간.getStationsInOrder())
                .containsExactly(광교역, 정자역, 강남역, 논현역, 신사역);
    }

}
