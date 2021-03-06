package org.factoriaf5.bftp2project4grupo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.factoriaf5.bftp2project4grupo1.repository.Game;
import org.factoriaf5.bftp2project4grupo1.repository.GameRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void loadsTheHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Autowired
    GameRepository gameRepository;

    @Test
    @WithMockUser
    void returnsTheExistingGames() throws Exception {

        Game game = gameRepository.save(new Game("Wii Sports", "Wii", 2006, "https://static.wikia.nocookie.net/videojuego/images/9/98/WiiSport_BA-1-.jpg/revision/latest/top-crop/width/360/height/450?cb=20070629185312", "Sci-fi", 19.99, 0, 19.99, "7"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("games", hasItem(game)));
    }

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void returnsAFormToAddNewGame() throws Exception {
        mockMvc.perform(get("/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add"))
                .andExpect(model().attributeExists("game"))
                .andExpect(model().attribute("title", "Add a new game"));
    }


    @Test
    @WithMockUser
    void allowsToCreateAGame() throws Exception {
        mockMvc.perform(post("/games/add")
                        .param("title", "Harry Potter and the Philosopher's Stone")
                        .param("platform", "J.K. Rowling")
                        .param("year", "2006")
                        .param("imageUrl", "google.com")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
        ;

        List<Game> existingGames = (List<Game>) gameRepository.findAll();
        assertThat(existingGames, contains(allOf(
                hasProperty("title", equalTo("Harry Potter and the Philosopher's Stone")),
                hasProperty("platform", equalTo("J.K. Rowling")),
                hasProperty("year", equalTo(2006)),
                hasProperty("imageUrl", equalTo("google.com"))
        )));
    }

    @Test
    @WithMockUser
    void returnsAFormToEditGames() throws Exception {
        Game game = gameRepository.save(new Game("Harry Potter and the Philosopher's Stone", "J.K. Rowling", 2006, "www.google.es", "Action", 19.99, 0, 19.99, "7"));
        mockMvc.perform(get("/games/add/" + game.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("/add"))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("title", "Edit game"));
    }

    @Test
    @WithMockUser
    void allowsToDeleteAGame() throws Exception {
        Game game = gameRepository.save(new Game("Harry Potter and the Philosopher's Stone", "J.K. Rowling", 2006, "www.google.es", "Sci-fi", 19.99, 0, 19.99, "12"));
        mockMvc.perform(get("/games/delete/" + game.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(gameRepository.findById(game.getId()), equalTo(Optional.empty()));
    }

    @Test
    @WithMockUser
    void returnsGamesFromAGivenCategory() throws Exception {

        Game fantasyGame = gameRepository.save(new Game("Super Mario", "GB", 1997, "https://www.lavanguardia.com/files/image_449_220/uploads/2020/09/12/5faa727a54ec2.png", "fantasy", 19.99, 0, 19.99, "3"));
        Game softwareGame = gameRepository.save(new Game("Wii Sports", "X360", 2002, "https://www.lavanguardia.com/files/image_449_220/uploads/2020/09/12/5faa727a54ec2.png", "Sci-fi", 19.99, 0, 19.99,"12"));

        mockMvc.perform(get("/?category=fantasy"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("games", hasItem(fantasyGame)))
                .andExpect(model().attribute("games", not(hasItem(softwareGame))));
    }

    @Test
    @WithMockUser
    void returnsGamesFromAGivenTitle() throws Exception {

        Game nameTitle = gameRepository.save(new Game("Super Mario", "GB", 1997, "https://www.lavanguardia.com/files/image_449_220/uploads/2020/09/12/5faa727a54ec2.png", "fantasy", 19.99, 0, 19.99, "7"));

        mockMvc.perform(get("/?title=Super Mario"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("games", hasItem(nameTitle)));
    }

    @Test
    @WithMockUser
    void returnsGamesFromAGivenPegi() throws Exception {

        Game contentDescriptor = gameRepository.save(new Game("Super Mario", "GB", 1997, "https://www.lavanguardia.com/files/image_449_220/uploads/2020/09/12/5faa727a54ec2.png", "fantasy", 19.99,0, 19.99, "7"));

        mockMvc.perform(get("/?pegi=7"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("games", hasItem(contentDescriptor)));
    }

    @Test
    @WithMockUser
    void returnsPriceWithDiscount() throws Exception {

        Game game = gameRepository.save(new Game("Sims", "GB", 1997,
                "https://www.lavanguardia.com/files/image_449_220/uploads/2020/09/12/5faa727a54ec2.png",
                "fantasy", 14.99,0, 14.99, "7"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("games", hasItem(game)));

        List<Game> existingGames = (List<Game>) gameRepository.findAll();
        assertThat(existingGames, contains(allOf(
                hasProperty("title", equalTo("Sims")),
                hasProperty("platform", equalTo("GB")),
                hasProperty("year", equalTo(1997)),
                hasProperty("imageUrl", equalTo("https://www.lavanguardia.com/files/image_449_220/uploads/2020/09/12/5faa727a54ec2.png")),
                hasProperty("category", equalTo("fantasy")),
                hasProperty("price", equalTo(14.99)),
                hasProperty("pegi", equalTo("7")),
                hasProperty("contentDescriptor", equalTo(""))

        )));

    }

    @Test
    void anonymousUsersCannotCreateAGame() throws Exception {
        mockMvc.perform(post("/")
                        .param("title", "Super Mario")
                        .param("platform", "NES")
                        .param("category", "Fantasy")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/login"));
    }



}
