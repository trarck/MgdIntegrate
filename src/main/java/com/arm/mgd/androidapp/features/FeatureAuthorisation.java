package com.arm.mgd.androidapp.features;

import android.util.Log;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class FeatureAuthorisation {

    private static final String AUTHORISATION_DATE_PROPERTY_NAME = "authorisationdate";

    private static final String AUTHORISATION_FILENAME = "authorisation.properties";

    private static final String TAG = "FeatureAuthorisation";

    private static FeatureAuthorisation singleton;

    private final DateFormat dateFormat = DateFormat.getDateInstance();

    private final EnumMap<FeatureAuthorisation.Feature, FeatureAuthorisation.Authorisation> featureAuthorisationMap = new EnumMap(FeatureAuthorisation.Feature.class);

    private FeatureAuthorisation.FeaturesAuthorisedChangedListener listener;

    private final IPropertiesProvider propertiesProvider;

    public FeatureAuthorisation( IPropertiesProvider var1) {
        this.propertiesProvider = var1;
        var1.setFilename("authorisation.properties");
        this.loadAuthorisations();
    }

    private long getDaysRemaining(Date var1) {
        Calendar var6 = Calendar.getInstance();
        var6.setTime(var1);
        var6.add(5, 30);
        long var2 = var6.getTime().getTime();
        long var4 = (new Date()).getTime();
        return TimeUnit.DAYS.convert(var2 - var4, TimeUnit.MILLISECONDS);
    }

    private void loadAuthorisations() {
        Properties var4;
        try {
            var4 = this.propertiesProvider.getProperties();
        } catch (IPropertiesProvider.PropertyStorageException var9) {
            Log.e("FeatureAuthorisation", "Failed to load authorisations from file: " + var9.getMessage());
            var4 = new Properties();
        } catch (FileNotFoundException var10) {
            var4 = new Properties();
        }

        Calendar var6 = null;
        String var7 = var4.getProperty("authorisationdate");
        Date var5 = null;
        if (var7 != null) {
            try {
                var5 = this.dateFormat.parse(var7);
            } catch (ParseException var8) {
                Log.e("FeatureAuthorisation", "Couldn't parse authorisation file date.");
                var5 = var6.getTime();
            }
        }

        var6 = Calendar.getInstance();
        var6.add(5, -30);
        Date var12 = var6.getTime();
        boolean var2 = false;
        boolean var1 = var2;
        if (var5 != null) {
            var1 = var2;
            if (var5.after(var12)) {
                var1 = true;
            }
        }

        FeatureAuthorisation.Feature[] var13 = FeatureAuthorisation.Feature.values();
        int var3 = var13.length;

        for(int var11 = 0; var11 < var3; ++var11) {
            FeatureAuthorisation.Feature var14 = var13[var11];
            if (Boolean.parseBoolean(var4.getProperty(var14.name(), "false"))) {
                if (var1) {
                    this.featureAuthorisationMap.put(var14, new FeatureAuthorisation.Authorisation(true, "Persistent authorisation found on device (" + this.getDaysRemaining(var5) + " days remaining)."));
                } else {
                    this.featureAuthorisationMap.put(var14, new FeatureAuthorisation.Authorisation(false, "Persistent authorisation found on device but the 30 day limit has expired. Enable the daemon and connect the MGD host to check for license information."));
                }
            } else {
                this.featureAuthorisationMap.put(var14, new FeatureAuthorisation.Authorisation(false, "No authorisation found. Enable the daemon and connect the MGD host to check for license information."));
            }
        }

    }

    private void notifyListener() {
        if (this.listener != null) {
            this.listener.featuresAuthorisedChanged();
        }

        this.storeAuthorisations();
    }

    private void storeAuthorisations() {
        Date var2 = new Date();
        Properties var1 = new Properties();
        var1.put("authorisationdate", this.dateFormat.format(var2));
        Iterator var5 = this.featureAuthorisationMap.entrySet().iterator();

        while(var5.hasNext()) {
            Entry var3 = (Entry)var5.next();
            if (((FeatureAuthorisation.Authorisation)var3.getValue()).allowed) {
                var1.put(((FeatureAuthorisation.Feature)var3.getKey()).name(), Boolean.toString(true));
            }
        }

        try {
            this.propertiesProvider.storeProperties(var1);
        } catch (IPropertiesProvider.PropertyStorageException var4) {
            Log.e("FeatureAuthorisation", "Failed to store authorisations to file: " + var4.getMessage());
        }
    }

    public FeatureAuthorisation.Authorisation getAuthorisation( FeatureAuthorisation.Feature var1) {
        return (FeatureAuthorisation.Authorisation)this.featureAuthorisationMap.get(var1);
    }

    public void setAuthorisation( FeatureAuthorisation.Feature var1,  FeatureAuthorisation.Authorisation var2) {
        this.featureAuthorisationMap.put(var1, var2);
        this.notifyListener();
    }

    public void setListener( FeatureAuthorisation.FeaturesAuthorisedChangedListener var1) {
        this.listener = var1;
    }

    public static class Authorisation {
        private final boolean allowed;

        private final String reason;

        public Authorisation(boolean var1,  String var2) {
            this.allowed = var1;
            this.reason = var2;
        }


        public String getReason() {
            return this.reason;
        }

        public boolean isAllowed() {
            return this.allowed;
        }
    }

    public static enum Feature {
        FULL_TRACE_REPLAY("Full Trace Replay");


        private final String featureName;

        private Feature( String var3) {
            this.featureName = var3;
        }


        public String getFeatureName() {
            return this.featureName;
        }
    }

    public interface FeaturesAuthorisedChangedListener {
        void featuresAuthorisedChanged();
    }
}
