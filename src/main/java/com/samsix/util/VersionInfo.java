/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;

import java.util.Map;

public interface VersionInfo
{
    public abstract String getVersion();

    public abstract Map<String, String> getVersionMap();
}
