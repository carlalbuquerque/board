package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.BoardColumnQueryService;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.CardQueryService;
import br.com.dio.service.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");


    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Bem vindo ao Board %s, selecione a operação desejada\n", entity.getId());

            var optional = -1;
            while (optional != 9) {
                System.out.println("1 - Criar um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear um card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - cancelar um card");
                System.out.println("6 - Ver board");
                System.out.println("7 - Ver coluna com cards");
                System.out.println("8 - Ver card");
                System.out.println("9 - voltar para o menu anterior um card");
                System.out.println("10 - Sair");
                int option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> System.out.println("Voltando para o menu anterior");
                    case 10 -> System.exit(0);
                    default -> System.out.println("Opção inválida, informe uma opção do menu");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

        private void createCard () throws SQLException {
            var card = new CardEntity();
            System.out.println("Informe o título do card:");
            card.setTitle(scanner.next());
            System.out.println("Informe a descrição do card");
            card.setDescription(scanner.next());
            card.setBoardColumn(entity.getInitialColumn());

            try (var connection = getConnection()) {
                new CardService(connection).create(card);
            }
        }


        private void moveCardToNextColumn()throws SQLException {
        System.out.println("Informe o id do card que deseja mover para a próxima coluna");
        var cardId =  scanner.nextLong();
        var boardColumnsInfo  = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId() , bc.getOrder() , bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).moveCardToNextColumn(cardId, boardColumnsInfo);
        }catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }

    }

    private void blockCard()throws SQLException {
        System.out.println("informe o id que será bloqueado");
        var cardId = scanner.nextLong();
        System.out.println("informe o motivo do bloqueio do card");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason , boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }



    private void unblockCard() throws SQLException {
        System.out.println("informe o id do card será desbloqueado");
        var cardId = scanner.nextLong();

        System.out.println("informe o motivo do desbloqueio do card");
        var reason = scanner.next();

        try(var connection = getConnection()){
            new CardService(connection).unblock(cardId,reason);
        }catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void cancelCard() throws SQLException {
            System.out.println("Informe o id do card que deseja mover para a coluna de cancelamento");
            var cardId = scanner.nextLong();
            var cancelColumns = entity.getCancelColumn();
            var cancelColumnsInfo = entity.getBoardColumns().stream()
                    .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(),bc.getKind()))
                    .toList();
            try(var connection = getConnection()){
                new CardService(connection).cancel(cardId, cancelColumns.getId() , cancelColumnsInfo);
            }catch (RuntimeException ex){
                System.out.println(ex.getMessage());
            }

        }


        private void showBoard() throws SQLException {
            try (var connection = getConnection()) {
                var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
                optional.ifPresent(b -> {
                    System.out.printf("Board [%s - %s]\n", b.id(), b.name());
                    b.columns().forEach(c ->
                            System.out.printf("Coluna [%s] Tipo: [%s] tem %d cards\n", c.name(), c.kind(), c.cardsAmount())
                    );
                });
            }
        }


        private void showColumn() throws SQLException {
            var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
            var selectedColumnId = - 1L;
            while (!columnsIds.contains(selectedColumnId)) {
                System.out.printf("Escolha uma coluna do board %s pelo id\n", entity.getName());
                entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]", c.getId(), c.getName(), c.getKind()));
                selectedColumnId = scanner.nextLong();
            }
            try(var connection = getConnection()){
                var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
                column.ifPresent(co -> {
                    System.out.printf("Coluna %s tipo %s\n" , co.getName() , co.getName() ,co.getKind());
                    co.getCards().forEach(ca -> System.out.printf(" card %s - %s \nDescrição: %s",
                            ca.getId(), ca.getTitle(), ca.getDescription())
                );
                });

            }
    }

    private void showCard() throws SQLException {
        System.out.println("informe o id do card que deseja visualizar");
        var selectedCardId = scanner.nextLong();
        try(var connection = getConnection()){
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c ->{
                                System.out.printf("card %s - %s.\n" , c.id(), c.title());
                                System.out.printf("descrição: %s\n" , c.description());
                                System.out.println(c.blocked() ?
                                        "esta bloqueado. motivo:" + c.blockedReason() :
                                        "não está bloqueado");
                                System.out.printf("Já foi bloqueado %s vezes\n" , c.blocksAmount());
                                System.out.printf("Está no momento na coluna %s - %s\n", c.columnId(), c.columnName());

                            },
                            ()-> System.out.printf("Não existe um card com o id %s\n" , selectedCardId));

        }
    }

}

