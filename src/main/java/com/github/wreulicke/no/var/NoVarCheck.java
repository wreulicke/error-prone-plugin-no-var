package com.github.wreulicke.no.var;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.VariableTree;
import java.util.List;

/** No var! */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "No var!, Use explicit types",
    tags = {"no-var"},
    severity = ERROR,
    link = "github.com/wreulicke/errorprone-plugin-no-var",
    linkType = BugPattern.LinkType.CUSTOM)
public class NoVarCheck extends BugChecker implements VariableTreeMatcher {

  @Override
  @SuppressWarnings("TreeToString")
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (declaredUsingVar(tree, state)) {
      SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
      fixBuilder.replace(
          tree,
          String.format(
              "%s %s = %s;",
              SuggestedFixes.qualifyType(state, fixBuilder, ASTHelpers.getSymbol(tree.getType())),
              tree.getName(),
              state.getSourceForNode(tree.getInitializer())));
      return buildDescription(tree)
          .addFix(fixBuilder.build())
          .setMessage("Should declare with explicit types")
          .build();
    }
    return Description.NO_MATCH;
  }

  private boolean declaredUsingVar(VariableTree tree, VisitorState state) {
    List<ErrorProneToken> tokens = state.getTokensForNode(tree);
    return tokens.size() > 0 && tokens.get(0).name().toString().equals("var");
  }
}
