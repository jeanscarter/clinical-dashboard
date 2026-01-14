package com.cms.presenter.events;

import com.cms.domain.ClinicalHistory;

public class HistorySavedEvent {
    private final ClinicalHistory history;

    public HistorySavedEvent(ClinicalHistory history) {
        this.history = history;
    }

    public ClinicalHistory getHistory() {
        return history;
    }
}
