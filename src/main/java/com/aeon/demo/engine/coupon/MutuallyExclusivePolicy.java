package com.aeon.demo.engine.coupon;

import java.util.*;

/**
 * 互斥组策略：将「groupId -> [templateId...]」转换为「templateId -> 互斥templateId集合」。
 *
 * @author codex
 */
public class MutuallyExclusivePolicy {

    private final Map<Integer, Set<Integer>> templateToExclusiveTemplates;

    public MutuallyExclusivePolicy(Map<Integer, List<Integer>> groups) {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        if (groups != null) {
            for (List<Integer> templates : groups.values()) {
                if (templates == null || templates.size() <= 1) {
                    continue;
                }
                for (Integer t : templates) {
                    if (t == null) {
                        continue;
                    }
                    Set<Integer> ex = map.computeIfAbsent(t, k -> new HashSet<>());
                    for (Integer other : templates) {
                        if (other != null && !other.equals(t)) {
                            ex.add(other);
                        }
                    }
                }
            }
        }
        this.templateToExclusiveTemplates = Collections.unmodifiableMap(map);
    }

    public Set<Integer> getExclusiveTemplateIds(int templateId) {
        Set<Integer> ex = templateToExclusiveTemplates.get(templateId);
        if (ex == null) {
            return Collections.emptySet();
        }
        return ex;
    }

    public boolean isMutuallyExclusive(int aTemplateId, int bTemplateId) {
        return getExclusiveTemplateIds(aTemplateId).contains(bTemplateId);
    }

    public boolean isExclusiveWithAny(int templateId, Collection<Integer> others) {
        if (others == null || others.isEmpty()) {
            return false;
        }
        Set<Integer> ex = getExclusiveTemplateIds(templateId);
        if (ex.isEmpty()) {
            return false;
        }
        for (Integer other : others) {
            if (other != null && ex.contains(other)) {
                return true;
            }
        }
        return false;
    }
}

