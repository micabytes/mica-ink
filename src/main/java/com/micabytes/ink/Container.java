package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Container extends Content {
  int level;
  @Nullable Container parent;
  List<Content> content = new ArrayList<>();
  @Nullable private String background = null;

  public void add(Content item) {
    content.add(item);
  }

  Container getContainer(int lvl) {
    Container c = this;
    while (c.level > lvl && c.parent != null) {
      c = c.parent;
    }
    return c;
  }

  @Override
  public String generateId(Container p) {
    if (id != null) return id;
    id = parent != null
        ? parent.getId() + InkParser.DOT + Integer.toString(parent.getContentIndex(this))
        : super.generateId(p);
    return id;
  }

  public int getContentSize() {
    return content.size();
  }

  public Content getContent(int i) {
    return content.get(i);
  }

  public int getContentIndex(Content c) {
    return content.indexOf(c);
  }

  public void initialize(Story story, Content c) throws InkRunTimeException {
    increment();
  }

  public void setBackground(@Nullable String img) {
    background = img;
  }

  @Nullable
  public String getBackground() {
    return background;
  }

}
