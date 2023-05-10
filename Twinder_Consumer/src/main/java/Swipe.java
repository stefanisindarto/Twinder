public class Swipe {

  private Integer swiper;
  private Integer swipee;
  private String comment;
  private String leftorright;


  public Swipe(Integer swiper, Integer swipee, String comment, String leftorright) {
    this.swiper = swiper;
    this.swipee = swipee;
    this.comment = comment;
    this.leftorright = leftorright;

  }

  public Integer getSwiper() {
    return swiper;
  }

  public void setSwiper(Integer swiper) {
    this.swiper = swiper;
  }

  public Integer getSwipee() {
    return swipee;
  }

  public void setSwipee(Integer swipee) {
    this.swipee = swipee;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getLeftorright() {return leftorright;}

  public void setLeftorright(String leftorright) {this.leftorright = leftorright;}
}