package com.arturlogan.board_dio.ui;

import com.arturlogan.board_dio.persistence.entity.BoardColumnEntity;
import com.arturlogan.board_dio.persistence.entity.BoardColumnKindEnum;
import com.arturlogan.board_dio.persistence.entity.BoardEntity;
import com.arturlogan.board_dio.services.BoardQueryService;
import com.arturlogan.board_dio.services.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.arturlogan.board_dio.persistence.config.ConnectionConfig.getConnection;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void execute() throws SQLException {
        System.out.println("Bem vindo ao gerenciador de boards. Escolha uma opção desejada");
        var option = 1;
        while (true){
            System.out.println("1 - Criar um novo board");
            System.out.println("2 - Selecionar um board existente");
            System.out.println("3 - Excluir um board");
            System.out.println("4 - Sair");

            option = scanner.nextInt();
            switch (option){
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);

                default -> System.out.println("opção inválida. Informe uma opção do menu");
            }
        }
    }

    private void createBoard() throws SQLException{
        var entity = new BoardEntity();

        System.out.println("Informe o nome do seu board");
        entity.setName(scanner.next());

        System.out.println("Seu board terá colunas além das 3 padrões? Se sim informe quantas, se não digite 0.");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da coluna inicial do board");
        var initialColumnName = scanner.next();
        var initialCollumn = createColumn(initialColumnName, BoardColumnKindEnum.INITIAL, 0);
        columns.add(initialCollumn);

        for (int i = 0; i < additionalColumns; i++){
            System.out.println("Informe o nome de tarfeas pendentes do board");
            var pendingColumnName = scanner.next();
            var pendingCollumn = createColumn(pendingColumnName, BoardColumnKindEnum.PENDING, i + 1);
            columns.add(pendingCollumn);
        }

        System.out.println("Informe o nome da coluna final");
        var finalColumnName = scanner.next();
        var finalCollumn = createColumn(finalColumnName, BoardColumnKindEnum.PENDING, additionalColumns + 1);
        columns.add(finalCollumn);

        System.out.println("Informe o nome da coluna de cancelamento do board");
        var cancelColumnName = scanner.next();
        var cancelCollumn = createColumn(cancelColumnName, BoardColumnKindEnum.CANCEL, additionalColumns + 1);
        columns.add(cancelCollumn);

        entity.setBoardColumns(columns);
        try (var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }
    }

    private void selectBoard() throws SQLException {
        System.out.println("Informe o id do board que será selecionado");

        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> System.out.printf("Não foi encontrado um board com o id %s\n", id)
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o id do board que será excluido");
        var id = scanner.nextLong();
        try (var connection = getConnection()){
            var service = new BoardService(connection);
            if (service.delete(id)){
                System.out.printf("O board %s foi excluido \n", id);
            } else {
                System.out.printf("Não foi encontrado um board com o id %s\n", id);
            }
        }
    }

    private BoardColumnEntity createColumn (final String name, final BoardColumnKindEnum kind, final int order){
    var boardColumn = new BoardColumnEntity();

    boardColumn.setName(name);
    boardColumn.setKind(kind);
    boardColumn.setOrder(order);
    return boardColumn;
    }
}
