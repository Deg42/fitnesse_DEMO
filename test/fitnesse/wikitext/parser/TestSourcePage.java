package fitnesse.wikitext.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TestSourcePage implements SourcePage {
  public String content;
  public HashMap<String, String> properties = new HashMap<>();
  public SourcePage includedPage;
  public String targetPath;

  public TestSourcePage withContent(String content) {
    this.content = content;
    return this;
  }

  public TestSourcePage withProperty(String key, String value) {
    properties.put(key, value);
    return this;
  }

  public TestSourcePage withIncludedPage(SourcePage includedPage) {
    this.includedPage = includedPage;
    return this;
  }

  public TestSourcePage withTarget(String targetPath) {
    this.targetPath = targetPath;
    return this;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getFullName() {
    return "fullname";
  }

  @Override
  public String getPath() {
    return null;
  }

  @Override
  public String getFullPath() {
    return null;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public boolean targetExists(String wikiWordPath) {
    return !targetPath.contains("Non");
  }

  @Override
  public String makeFullPathOfTarget(String wikiWordPath) {
    return targetPath;
  }

  @Override
  public String findParentPath(String targetName) {
    return null;
  }

  @Override
  public Maybe<SourcePage> findIncludedPage(String pageName) {
    return includedPage != null ? new Maybe<>(includedPage) : Maybe.nothingBecause("missing");
  }

  @Override
  public Collection<SourcePage> getChildren() {
    return null;
  }

  @Override
  public boolean hasProperty(String propertyKey) {
    return properties.containsKey(propertyKey);
  }

  @Override
  public String getProperty(String propertyKey) {
    return properties.getOrDefault(propertyKey, "");
  }

  @Override
  public int compareTo(SourcePage other) {
    return getName().compareTo(other.getName());
  }
}
