package resolvers.impl;

import model.entity.MovieEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import parsers.Parser;
import parsers.impl.KinopoiskParser;
import resolvers.CommandResolver;
import service.MoviesService;
import service.impl.MoviesServiceImpl;
import service.sessions.SessionManager;
import service.statemachine.State;

import utils.TelegramBotUtils;


public class AddMovieCommandResolver implements CommandResolver {

    private final Parser parser;
    private final MoviesService moviesService;
    private final String COMMAND_NAME = "/add";

    public AddMovieCommandResolver() {
        this.parser = new KinopoiskParser();
        this.moviesService = new MoviesServiceImpl();
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public void resolveCommand(TelegramLongPollingBot tg_bot, String text, Long chat_id) {

        if (text.startsWith("/add")) {
            TelegramBotUtils.sendMessage(tg_bot, """
                    Введите ссылку с кинопоиска
                    """, chat_id);
            setState(chat_id, State.ADD);
        } else {
            
            if(!text.matches("https://www.kinopoisk.ru/film/\\d+[/]*")) {
                TelegramBotUtils.sendMessage(tg_bot, """
                        Не похоже на ссылку с кинопоиска :)
                        """, chat_id);
                TelegramBotUtils.sendMessage(tg_bot, """
                        Может, ввести что-нибудь более читабельное? 😉
                        """, chat_id);
                return;
            }
            
            MovieEntity movie = parser.parse(text);
            
            if(movieAlreadyRegisteredInUsersMoviesDB(movie, chat_id)) {
                TelegramBotUtils.sendMessage(tg_bot, """
                    Такое кино вы уже добавляли!)
                    """, chat_id);
            }
            
            moviesService.saveMovie(movie, chat_id);
            TelegramBotUtils.sendMessage(tg_bot, """
                    Поздравляем! Фильм успешно добавлен в базу данных!!!!
                    """, chat_id);
            setState(chat_id, State.IDLE);
        }

    }
    
    private boolean movieAlreadyRegisteredInUsersMoviesDB(MovieEntity movie, Long chat_id) {
        return moviesService.movieRegisteredInUsersMovies(movie, chat_id);
    }
    
    private void setState(Long chatId, State state) {
        SessionManager.getInstance().getSession(chatId).setState(state);
    }
    
}
