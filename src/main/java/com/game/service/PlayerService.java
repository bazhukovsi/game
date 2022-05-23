package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PlayerService {
    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);

    Player createPlayer(final Player player);

    Player getPlayerById(Long id);

    Player updatePlayer(Player newPlayer, Long id) throws IllegalArgumentException;

    void deletePlayer(Long id);

    List<Player> prepareFilteredPlayers(final List<Player> filteredShips, PlayerOrder order,
                                      Integer pageNumber, Integer pageSize);

    List<Player> getPlayersList(String name, String title, Race race, Profession profession,
                                Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                                Integer minLevel, Integer maxLevel);
}

