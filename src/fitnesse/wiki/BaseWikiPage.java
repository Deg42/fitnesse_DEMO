// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.WikiSourcePage;

public abstract class BaseWikiPage implements WikiPage, WikitextPage {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final WikiPage parent;
  private final VariableSource variableSource;
  private ParsingPage parsingPage;
  private Symbol syntaxTree;

  protected BaseWikiPage(String name, VariableSource variableSource) {
    this(name, null, variableSource);
  }

  protected BaseWikiPage(String name, WikiPage parent) {
    this(name, parent, parent instanceof BaseWikiPage ? ((BaseWikiPage) parent).variableSource : null);
  }

  protected BaseWikiPage(String name, WikiPage parent, VariableSource variableSource) {
    this.name = name;
    this.parent = parent;
    this.variableSource = variableSource;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl(this);
  }

  @Override
  public WikiPage getParent() {
    return parent == null ? this : parent;
  }

  @Override
  public boolean isRoot() {
    return parent == null || parent == this;
  }

  protected VariableSource getVariableSource() {
    return variableSource;
  }

  @Override
  public String getVariable(String name) {
    ParsingPage parsingPage = getParsingPage();
    Maybe<String> variable = parsingPage.findVariable(name);
    if (variable.isNothing()) return null;

    Parser parser = Parser.make(parsingPage, "", SymbolProvider.variableDefinitionSymbolProvider);
    return new HtmlTranslator(null, parsingPage).translate(parser.parseWithParent(variable.getValue(), null));
  }

  @Override
  public String getHtml() {
    return new HtmlTranslator(new WikiSourcePage(this), getParsingPage()).translateTree(getSyntaxTree());
  }

  @Override
  public ParsingPage getParsingPage() {
    parse();
    return parsingPage;
  }

  @Override
  public Symbol getSyntaxTree() {
    parse();
    return syntaxTree;
  }

  private void parse() {
    if (syntaxTree == null) {
      // This is the only page where we need a VariableSource
      WikiSourcePage sourcePage = new WikiSourcePage(this);
      ParsingPage.Cache cache = new ParsingPage.Cache();
      VariableSource compositeVariableSource = new ParsingPage.CompositeVariableSource(
              new ParsingPage.ApplicationVariableSource(variableSource),
              new ParsingPage.PageVariableSource(sourcePage),
              new UserVariableSource(variableSource),
              cache,
              new ParentPageVariableSource(),
              variableSource);
      parsingPage = new ParsingPage(sourcePage, compositeVariableSource, cache);
      syntaxTree = Parser.make(parsingPage, getData().getContent()).parse();
    }
  }

  protected void resetCache() {
    parsingPage = null;
    syntaxTree = null;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + ": " + name;
  }

  @Override
  public int compareTo(Object o) {
    try {
      return getPageCrawler().getFullPath().compareTo(((WikiPage) o).getPageCrawler().getFullPath());
    }
    catch (Exception e) {
      return 0;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof WikiPage))
      return false;
    try {
      return getPageCrawler().getFullPath().equals(((WikiPage) o).getPageCrawler().getFullPath());
    }
    catch (Exception e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    try {
      return getPageCrawler().getFullPath().hashCode();
    }
    catch (Exception e) {
      return 0;
    }
  }

  public static class UserVariableSource implements VariableSource {

    private final VariableSource variableSource;

    public UserVariableSource(VariableSource variableSource) {
      this.variableSource = variableSource;
    }

    @Override
    public Maybe<String> findVariable(String name) {
      if(variableSource instanceof UrlPathVariableSource){
        Maybe<String> result = ((UrlPathVariableSource) variableSource).findUrlVariable(name);
        if (!result.isNothing()) return result;
      }
      return Maybe.noString;
    }
  }

  public class ParentPageVariableSource implements VariableSource {
    @Override
    public Maybe<String> findVariable(String name) {
      if (BaseWikiPage.this.isRoot()) {
        return Maybe.noString;
      }
      WikiPage parentPage = BaseWikiPage.this.getParent();
      if (parentPage instanceof WikitextPage) {
        return ((WikitextPage) parentPage).getParsingPage().findVariable(name);
      } else {
        String value = parentPage.getVariable(name);
        return value != null ? new Maybe<String>(value) : Maybe.noString;
      }
    }
  }
}
