public class Selection {
  String name;
  int start;
  int end;
  String type;
  String regex;
  String description;

  @Override
  public String toString() {
    return "Name:" + name + " start:" + start + " end:" + end;
  }
}
