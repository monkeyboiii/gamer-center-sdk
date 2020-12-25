package model;

public class Game {

    private Long id;
    private String name;

    private Double price;
    private Double score;
    private String description;

    private Long developerId;
    private String tag;
    private String branch;

    private String front_image;


    //


    public Game(Game game) {
        id = game.getId();
        name = game.getName();
        price = game.getPrice();
        score = game.getScore();
        description = game.getDescription();
        developerId = game.getDeveloperId();
        tag = game.getTag();
        branch = game.getBranch();
        front_image = game.getFront_image();
    }

    public Game() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getFront_image() {
        return front_image;
    }

    public void setFront_image(String front_image) {
        this.front_image = front_image;
    }
}
