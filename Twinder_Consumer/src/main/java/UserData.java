public class UserData {
  private Integer swiper;
  private Integer likeCount;
  private Integer dislikeCount;

  public UserData(Integer swiper) {
    this.swiper = swiper;
    this.likeCount = 0;
    this.dislikeCount = 0;
  }

  public Integer getSwiper() {
    return swiper;
  }

  public void setSwiper(Integer swiper) {
    this.swiper = swiper;
  }

  public Integer getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
  }

  public Integer getDislikeCount() {
    return dislikeCount;
  }

  public void setDislikeCount(Integer dislikeCount) {
    this.dislikeCount = dislikeCount;
  }
}
