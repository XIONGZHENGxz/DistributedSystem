class Record {

    private String name;
    private String book;
    private int id;

    Record(String name, String book, int id) {
        this.id = id;
        this.name = name;
        this.book = book;
    }

    String getName() {
        return name;
    }

    String getBook() {
        return book;
    }

    int getID() {
        return id;
    }

}