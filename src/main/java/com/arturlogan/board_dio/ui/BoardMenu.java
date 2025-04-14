package com.arturlogan.board_dio.ui;

import com.arturlogan.board_dio.dto.BoardColumnInfoDTO;
import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import com.arturlogan.board_dio.persistence.entity.BoardEntity;
import com.arturlogan.board_dio.persistence.entity.CardEntity;
import com.arturlogan.board_dio.services.BoardColumnQueryService;
import com.arturlogan.board_dio.services.BoardQueryService;
import com.arturlogan.board_dio.services.CardQueryService;
import com.arturlogan.board_dio.services.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

import static com.arturlogan.board_dio.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {

    private final BoardEntity entity;
    private final Scanner scanner = new Scanner(System.in);

    public void execute() {
        try {
            System.out.printf("Bem vindo ao board %s, selecione a operação desejada\n", entity.getId());
            int option = -1;
            while (option != 9) {
                System.out.println("1 - Criar um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear um card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - Cancelar um card");
                System.out.println("6 - Ver um board");
                System.out.println("7 - Ver colunas com cards");
                System.out.println("8 - Ver cards");
                System.out.println("9 - Voltar para o menu anterior");
                System.out.println("10 - Sair");

                try {
                    option = scanner.nextInt();
                    scanner.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("Entrada inválida. Informe um número.");
                    scanner.nextLine();
                    continue;
                }

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

                    default -> System.out.println("Opção inválida. Informe uma opção do menu.");
                }
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException{
        var card = new CardEntity();
        System.out.println("Informe o título do card");
        card.setTitle(scanner.nextLine());
        System.out.println("Informe a descrição do card");
        card.setDescription(scanner.nextLine());
        card.setBoardColumn(entity.getInitialColumn());
        try (var connection = getConnection()){
            new CardService(connection).insert(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Informe o id do card que deseja mover");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, entity.getId(), boardColumnsInfo); // Adicione entity.getId() aqui
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try(var connection = getConnection()){
            var optionalBoardDetails = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optionalBoardDetails.ifPresent(boardDetails -> {
                System.out.printf("Board [%s, %s]\n", boardDetails.id(), boardDetails.name());
                boardDetails.columns().forEach(columnDetail ->
                        System.out.printf("Coluna [%s] tipo: [%s] tem %s cards\n",
                                columnDetail.name(), columnDetail.kindEnum(), columnDetail.cardsAmount())
                );
            });
        }
    }

    private void unblockCard() {
        System.out.println("Implementar desbloquear card...");
    }

    private void blockCard() {
        System.out.println("Implementar bloquear card...");
    }

    private void cancelCard() {
        System.out.println("Implementar cancelar card...");
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumn = -1L;
        while (!columnsIds.contains(selectedColumn)){
            System.out.printf("Escolha uma coluna do board %s\n", entity.getName());
            entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]", c.getId(), c.getName(), c.getKind()));
            System.out.println();
            selectedColumn = scanner.nextLong();
        }
        try (var connection = getConnection()){
          var column =   new BoardColumnQueryService(connection).findById(selectedColumn);
          column.ifPresent(co -> {
              System.out.printf("Coluna %s tipo %s\n", co.getName(), co.getKind());
              System.out.println();
              co.getCards().forEach(ca -> System.out.printf("Card %s - %s\nDescrição: %s\n", ca.getId(), ca.getTitle(), ca.getDescription()));
          });
        }
    }

    private void showCard() throws SQLException{
        System.out.println("Informe o id do card que deseja visualizar: ");
        var selectedCardId = scanner.nextLong();
        try (var connection = getConnection()){
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(c -> {
                        System.out.println("--------------------------------------------------");
                        System.out.printf("Card %s - %s. \n", c.id(), c.title());
                        System.out.printf("Descrição: %s\n", c.description());
                        System.out.println(c.blocked() ? "Está bloqueado. Motivo: " + c.blockReason() :
                                "Não está bloqueado");
                        System.out.printf("Já foi bloqueado %s vezes", c.blocksAmount());
                        System.out.println();
                        System.out.printf("Está no momento na coluna %s - Nome: %s\n", c.columnId(), c.columnName());
                    }, () -> System.out.printf("Não existe um card com o id %s\n", selectedCardId));
            System.out.println("--------------------------------------------------");
        }
    }
}