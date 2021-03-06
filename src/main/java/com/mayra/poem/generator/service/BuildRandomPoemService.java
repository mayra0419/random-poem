package com.mayra.poem.generator.service;

import com.mayra.poem.generator.domain.dto.Action;
import com.mayra.poem.generator.domain.dto.Rule;
import com.mayra.poem.generator.util.RuleConstants;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BuildRandomPoemService {

    private static final String RULE_FOR_STARTING_A_POEM = "POEM";
    private static final Random RANDOM = new Random();
    private static final ThreadLocal<StringBuilder> RANDOM_POEM = new ThreadLocal<>();
    private final Map<String, Rule> poemRules;

    public BuildRandomPoemService(@Qualifier("poemRules") Map<String, Rule> poemRules) {
        this.poemRules = poemRules;
    }

    public String execute() {
        var firstRule = poemRules.get(RULE_FOR_STARTING_A_POEM);
        if (null == firstRule) {
            throw new ExceptionInInitializerError(
                "There is an error with the initial configuration");
        }
        RANDOM_POEM.set(new StringBuilder());
        for (Action action : firstRule.getActions()) {
            executeAction(action);
        }
        var finishedRandomPoem = RANDOM_POEM.toString();
        RANDOM_POEM.remove();
        return finishedRandomPoem.trim();
    }

    private void executeAction(Action action) {
        if (null != action.getWords() && action.getWords().length > 0) {
            var rnd = RANDOM.nextInt(action.getWords().length);
            RANDOM_POEM.get().append(action.getWords()[rnd]).append(" ");
            return;
        }
        if (null != action.getAssociatedRules() && action.getAssociatedRules().length > 0) {
            var rnd = RANDOM.nextInt(action.getAssociatedRules().length);
            var selectedRule = action.getAssociatedRules()[rnd];
            if (RuleConstants.KEYWORD_END.equals(selectedRule)) {
                return;
            }
            if (RuleConstants.KEYWORD_LINE_BREAK.equals(selectedRule)) {
                RANDOM_POEM.get().append(System.lineSeparator());
                return;
            }
            executeRule(selectedRule);
        }
    }

    private void executeRule(String ruleId) {
        var rule = poemRules.get(ruleId);
        for (Action action : rule.getActions()) {
            executeAction(action);
        }
    }
}
