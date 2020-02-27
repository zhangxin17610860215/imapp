package com.netease.wulewan.uikit.business.contact.core.item;

import java.io.Serializable;

public interface ContactItemFilter extends Serializable {
    boolean filter(AbsContactItem item);
}
