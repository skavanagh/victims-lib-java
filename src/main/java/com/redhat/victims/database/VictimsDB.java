package com.redhat.victims.database;

/*
 * #%L
 * This file is part of victims-lib.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import com.redhat.victims.VictimsConfig;
import com.redhat.victims.VictimsException;

/**
 * A class providing easy instantiation of DB implementation based on the
 * configured driver.
 * 
 * @author abn
 * 
 */
public class VictimsDB {
    /**
     * The default driver class to use.
     * 
     * @return
     */
    public static String defaultDriver() {
        return Driver.H2;
    }

    /**
     * Get the default url for a preconfigured driver.
     * 
     * @return
     */
    public static String defaultURL(String driver) {
        assert Driver.exists(driver);
        String home = "";
        try {
            home = VictimsConfig.home().toString();
        } catch (VictimsException e) {
            // Ignore and use cwd
        }
        return Driver.url(driver, FilenameUtils.concat(home, "victims"));
    }

    /**
     * The default url for the default driver.
     * 
     * @return
     */
    public static String defaultURL() {
        return defaultURL(defaultDriver());
    }

    public static String dbUpdate() {
        return VictimsConfig.dbUpdate();
    }

    /**
     * Fetches an instance implementing {@link VictimsDBInterface} using the
     * configured driver.
     * 
     * @return A {@link VictimsDBInterface} implementation.
     * @throws VictimsException
     */
    public static VictimsDBInterface db() throws VictimsException {
        String driver = VictimsConfig.dbDriver();
        String dbUrl = VictimsConfig.dbUrl();
        if (!driver.equals(defaultDriver())) {
            if (!Driver.exists(driver) && dbUrl.equals(defaultURL())) {
                // Custom drivers require custom urls
                throw new VictimsException(
                        "A custom JDBC driver was specified without setting "
                                + VictimsConfig.Key.DB_URL);
            }
        }
        return (VictimsDBInterface) new VictimsSqlDB();
    }

    /**
     * This class facilitates use of multiple driver classes
     * 
     * @author abn
     * 
     */
    public static class Driver {
        public static final String H2 = "org.h2.Driver";

        public static final HashMap<String, String> urls = new HashMap<String, String>();

        static {
            urls.put(H2, "jdbc:h2:%s;MVCC=true");
        }

        /**
         * Test if a given driver class is configured.
         * 
         * @param driver
         *            The driver class.
         * @return
         */
        public static boolean exists(String driver) {
            return urls.containsKey(driver);
        }

        /**
         * Get the default connection URL for a given driver.
         * 
         * @param driver
         *            The driver class.
         * @param path
         *            The path to the db on disk/network.
         * @return
         */
        public static String url(String driver, String path) {
            return String.format(urls.get(driver), path);
        }

        /**
         * Add a driver to use.
         * 
         * @param driver
         *            The driver class.
         * @param urlFormat
         *            The format of the connection url. Eg: jdbc:h2:%s;MVCC=true
         */
        public static void addDriver(String driver, String urlFormat) {
            urls.put(driver, urlFormat);
        }

    }

}
