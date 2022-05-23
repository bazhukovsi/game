package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.ex.BadRequestException;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {

    PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Вывод всех игроков
     *
     * @param name          - имя персонажа
     * @param title         - титул персонажа
     * @param race          - раса персонажа
     * @param profession    - профессия персонажа
     * @param after         -
     * @param before        -
     * @param banned        - забанен/не забанен
     * @param minExperience - опыт
     * @param maxExperience - опыт
     * @param minLevel      - уровень
     * @param maxLevel      - уровень
     * @param order         -
     * @param pageNumber    -
     * @param pageSize      -
     * @return
     */
    @GetMapping(path = "/rest/players")
    public List<Player> showAllPlayers(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false) Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false) Boolean banned,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                       @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                       @RequestParam(value = "order", required = false) PlayerOrder order,
                                       @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        List<Player> filteredPlayers = playerService.getPlayersList(name, title, race, profession, after, before,
                banned, minExperience, maxExperience, minLevel, maxLevel);
        return playerService.prepareFilteredPlayers(filteredPlayers, order, pageNumber, pageSize);
    }

    /**
     * Получение количества игроков
     */
    @GetMapping(path = "/rest/players/count")
    public Integer showCountAllPlayers(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false) Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false) Boolean banned,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                       @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        return playerService.getPlayersList(name, title, race, profession,
                after, before, banned, minExperience, maxExperience,
                minLevel, maxLevel).size();
    }

    /**
     * Получение игрока по ID
     */

    @GetMapping("/rest/players/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        if (!isIdValid(id)) {
            throw new BadRequestException();
        }
        return playerService.getPlayerById(id);
    }

    /**
     * Редактирование игрока
     */

    @PostMapping("/rest/players/{id}")
    public @ResponseBody
    Player updatePlayer(@RequestBody Player player, @PathVariable Long id) {
        if (!isIdValid(id)) {
            throw new BadRequestException();
        }
        return playerService.updatePlayer(player, id);
    }

    /**
     * - Создание игрока
     */

    @PostMapping("/rest/players")
    public @ResponseBody Player createPlayer (@RequestBody Player player) {
        Player createdPlayer = playerService.createPlayer(player);
        if (createdPlayer == null) {
            throw new BadRequestException();
        }
        return createdPlayer;
    }

    /**
     * Удаление игрока
     */

    @DeleteMapping("/rest/players/{id}")
    public void deletePlayer(@PathVariable Long id) {
        if (!isIdValid(id)) {
            throw new BadRequestException();
        }
        playerService.deletePlayer(id);
    }


    /**
     * Валидность ID
     * Не валидным считается id, если он:
     * - не числовой
     * - не целое число
     * - не положительный
     */

    private Boolean isIdValid(Long id) {
        if (id == null ||
                id != Math.floor(id) ||
                id <= 0) {
            return false;
        }
        return true;
    }

}
