package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.ex.BadRequestException;
import com.game.ex.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    PlayerRepository playerRepository;

    public PlayerServiceImpl() {
    }

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final int page = pageNumber == null ? 0 : pageNumber;
        final int size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }


    public Player createPlayer(Player player) {
        if (player == null ||
                player.getName() == null ||
                player.getName().isEmpty() ||
                player.getName().length() > 12 ||
                player.getTitle() == null ||
                player.getTitle().isEmpty() ||
                player.getTitle().length() > 30 ||
                player.getRace() == null ||
                player.getProfession() == null ||
                player.getBirthday().getTime() < 0 ||
                player.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2000 ||
                player.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3000 ||
                player.getExperience() == null ||
                player.getExperience() < 0 ||
                player.getExperience() > 10_000_000) {
            throw new BadRequestException();
        }
//        if (player.getBanned() == null) {
//            player.setBanned(false);
//        }
//        if (player.getBanned() == null) {
//            player.setBanned(false);
//        }
//        if (player.getBanned() != null && !player.getBanned()) {
//           player.setBanned(true);
//        }
        player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
        player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());

        return playerRepository.saveAndFlush(player);
    }

    @Override
    public Player getPlayerById(Long id) throws NotFoundException {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player updatePlayer(Player newPlayer, Long id) throws IllegalArgumentException {
        Player playerForUpdate = getPlayerById(id);
        if (newPlayer == null || playerForUpdate == null) {
            throw new BadRequestException();
        }
        if (newPlayer.getName() != null) {
            if (newPlayer.getName().isEmpty() || newPlayer.getName().length() > 12) {
                throw new BadRequestException();
            }
            playerForUpdate.setName(newPlayer.getName());
        }
        if (newPlayer.getTitle() != null) {
            if (newPlayer.getTitle().isEmpty() || newPlayer.getTitle().length() > 30) {
                throw new BadRequestException();
            }
            playerForUpdate.setTitle(newPlayer.getTitle());
        }
        if (newPlayer.getRace() != null) {
            playerForUpdate.setRace(newPlayer.getRace());
        }
        if (newPlayer.getProfession() != null) {
            playerForUpdate.setProfession(newPlayer.getProfession());
        }
        if (newPlayer.getBirthday() != null) {
            if (newPlayer.getBirthday().getTime() < 0 ||
                    newPlayer.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2000 ||
                    newPlayer.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3000) {
                throw new BadRequestException();
            }
            playerForUpdate.setBirthday(newPlayer.getBirthday());
        }
        if (newPlayer.getBanned() != null) {
            playerForUpdate.setBanned(newPlayer.getBanned());
        }
        if (newPlayer.getExperience() != null) {
            if (newPlayer.getExperience() < 0 || newPlayer.getExperience() > 10_000_000) {
                throw new BadRequestException();
            }
            playerForUpdate.setExperience(newPlayer.getExperience());
        }
        playerForUpdate.setLevel(executeLevel(playerForUpdate));
        playerForUpdate.setUntilNextLevel(executeUntilNextLevel(playerForUpdate));
        return playerRepository.saveAndFlush(playerForUpdate);
    }

    /**
     * Расчет характеристик перед сохранением при апдейте
     */

    private Integer executeLevel(Player player) {
        return (int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
    }

    private Integer executeUntilNextLevel(Player player) {
        return 50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience();
    }

    /**
     * Удаление игрока
     */

    @Override
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        playerRepository.deleteById(id);
    }

    /**
     * Фильтр
     */

    public List<Player> prepareFilteredShips(final List<Player> filteredPlayers, PlayerOrder order,
                                             Integer pageNumber, Integer pageSize) {
        pageNumber = pageNumber == null ? 0 : pageNumber;
        pageSize = pageSize == null ? 3 : pageSize;
        return filteredPlayers.stream()
                .sorted(getComparator(order))
                .skip((long) pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    private Comparator<Player> getComparator(PlayerOrder order) {
        if (order == null) {
            return Comparator.comparing(Player::getId);
        }
        Comparator<Player> comparator = null;
        if (order.getFieldName().equals("id")) {
            comparator = Comparator.comparing(Player::getId);
        } else if (order.getFieldName().equals("name")) {
            comparator = Comparator.comparing(Player::getName);
        } else if (order.getFieldName().equals("experience")) {
            comparator = Comparator.comparing(Player::getExperience);
        } else if (order.getFieldName().equals("birthday")) {
            comparator = Comparator.comparing(Player::getBirthday);
        } else if (order.getFieldName().equals("level")) {
            comparator = Comparator.comparing(Player::getLevel);
        }
        return comparator;
    }

    public List<Player> getPlayersList(String name, String title, Race race, Profession profession,
                                       Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience,
                                       Integer minLevel, Integer maxLevel) {
        List<Player> filteredPlayers = playerRepository.findAll();
        if (name != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (title != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getTitle().contains(title))
                    .collect(Collectors.toList());
        }
        if (race != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getRace().equals(race))
                    .collect(Collectors.toList());
        }
        if (profession != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getProfession().equals(profession))
                    .collect(Collectors.toList());
        }
        if (after != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getBirthday().after(new Date(after)))
                    .collect(Collectors.toList());
        }
        if (before != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getBirthday().before(new Date(before)))
                    .collect(Collectors.toList());
        }
        if (banned != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getBanned().equals(banned))
                    .collect(Collectors.toList());
        }
        if (minExperience != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getExperience() >= minExperience)
                    .collect(Collectors.toList());
        }
        if (maxExperience != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getExperience() <= maxExperience)
                    .collect(Collectors.toList());
        }
        if (minLevel != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getLevel() >= minLevel)
                    .collect(Collectors.toList());
        }
        if (maxLevel != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(player -> player.getLevel() <= maxLevel)
                    .collect(Collectors.toList());
        }
        return filteredPlayers;
    }
}
