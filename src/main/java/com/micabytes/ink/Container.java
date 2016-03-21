package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Container extends Content {
  String id;
  int level;
  @Nullable Container parent;
  List<Content> content = new ArrayList<>();

  public void add(Content item) {
    content.add(item);
  }

  public Container getContainer(int lvl) {
    Container c = this;
    while (c.level > lvl && c.parent != null) {
      c = c.parent;
    }
    return c;
  }

  public String getId() {
    return id;
  }

  public int getContentSize() {
    return content.size();
  }

  public Content getContent(int i) {
    return content.get(i);
  }

}
