/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.glassfish.common.nodes;

import java.awt.Component;
import java.awt.Image;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.actions.*;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2InstanceNode extends AbstractNode implements ChangeListener { //Node.Cookie {

    // Server instance icon
    private static final String ICON_BASE = 
            "org/netbeans/modules/glassfish/common/resources/server.gif"; // NOI18N
    
    // Icon badges for current server state
    private static final String WAITING_ICON
            = "org/netbeans/modules/glassfish/common/resources/waiting.png"; // NOI18N
    private static final String RUNNING_ICON 
            = "org/netbeans/modules/glassfish/common/resources/running.png"; // NOI18N
    private static final String DEBUGGING_ICON 
            = "org/netbeans/modules/glassfish/common/resources/debugging.png"; // NOI18N
    private static final String SUSPENDED_ICON
            = "org/netbeans/modules/glassfish/common/resources/suspended.png"; // NOI18N
    private static final String PROFILING_ICON
            = "org/netbeans/modules/glassfish/common/resources/profiling.png"; // NOI18N
    private static final String PROFILER_BLOCKING_ICON
            = "org/netbeans/modules/glassfish/common/resources/profilerblocking.png"; // NOI18N
    

    private final GlassfishInstance serverInstance;
    private final InstanceContent instanceContent;
    private final boolean isFullNode;
    private volatile String displayName = null;
    private volatile String shortDesc = null;

    public Hk2InstanceNode(final GlassfishInstance instance, boolean isFullNode) {
        this(instance, new InstanceContent(), isFullNode);
        if (isFullNode) {
            instance.getCommonSupport().refresh();
        }
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    private Hk2InstanceNode(final GlassfishInstance instance, final InstanceContent ic, boolean isFullNode) {
        super(isFullNode ? new Hk2InstanceChildren(instance) : Children.LEAF, 
                new ProxyLookup(new AbstractLookup(ic), instance.getLookup()));
        serverInstance = instance;
        instanceContent = ic;
        this.isFullNode = isFullNode;
        setIconBaseWithExtension(ICON_BASE);
        
        if(isFullNode) {
            serverInstance.getCommonSupport().addChangeListener(
                    WeakListeners.change(this, serverInstance));
            instanceContent.add(new RefreshModulesCookie() {
                @Override
                public RequestProcessor.Task refresh() {
                    return refresh(null, null);
                }

                @Override
                public RequestProcessor.Task refresh(String expected, String unexpected) {
                    Children children = getChildren();
                    if(children instanceof Refreshable) {
                        ((Refreshable) children).updateKeys();
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public String getDisplayName() {
        if(displayName == null) {
            displayName = buildDisplayName();
        }
        return displayName;
    }

    @Override
    public String getShortDescription() {
        if(shortDesc == null) {
            shortDesc = NbBundle.getMessage(Hk2InstanceNode.class, "LBL_ServerInstanceNodeDesc",
                    serverInstance.getServerDisplayName(),
                    getAdminUrl());
        }
        return shortDesc;
    }

    /** Get the set of actions that are associated with this node.
     * <p/>
     * This set is used to construct the context menu for the node.    
     * <p/>
     * @param context Whether to find actions for context meaning or for the
     *                node itself.
     * @return A list of actions (you may include nulls for separators).
     */
    @Override
    public Action[] getActions(boolean context) {
        if (!isFullNode) {
            return new Action[]{};
        }

        return serverInstance.isRemote()
                ? getRemoteActions() : getLocalActions();
    }

    /**
     * Node actions for local server instance.
     * <p/>
     * @return A list of actions for local server instance.
     */
    public Action[] getLocalActions() {
        return new Action[] {
            SystemAction.get(StartServerAction.class),
            SystemAction.get(DebugAction.class),
            SystemAction.get(ProfileAction.class),
            SystemAction.get(RestartAction.class),
            SystemAction.get(StopServerAction.class),
            SystemAction.get(RefreshModulesAction.class),
            null,
            SystemAction.get(RemoveServerAction.class),
            null,
            SystemAction.get(KillServerAction.class),
            null,
            SystemAction.get(ViewAdminConsoleAction.class),
            SystemAction.get(ViewServerLogAction.class),
            SystemAction.get(ViewUpdateCenterAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }

    /**
     * Node actions for local server instance.
     * <p/>
     * @return A list of actions for local server instance.
     */
    public Action[] getRemoteActions() {
        return new Action[] {
            SystemAction.get(StartServerAction.class),
            SystemAction.get(DebugAction.class),
            SystemAction.get(ProfileAction.class),
            SystemAction.get(RestartAction.class),
            SystemAction.get(StopServerAction.class),
            SystemAction.get(RefreshModulesAction.class),
            null,
            SystemAction.get(RemoveServerAction.class),
            null,
            SystemAction.get(ViewAdminConsoleAction.class),
            SystemAction.get(ViewServerLogAction.class),
            SystemAction.get(ViewUpdateCenterAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public Component getCustomizer() {
    //        CustomizerDataSupport dataSup = new CustomizerDataSupport(getDeploymentManager());
    //        return new Customizer(dataSup, new Hk2J2eePlatformFactory().getJ2eePlatformImpl(getDeploymentManager()));
        return new javax.swing.JPanel();
    }

    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }   
    
    /**
     * Copied along with icons from InstanceNodeDecorator in j2eeserver module.
     * 
     * @todo Could this be put in common server SPI to make it sharable?
     * 
     * @param origImg
     * @return
     */
    private Image badgeIcon(Image origImg) {
        Image badge = null;        
        switch (serverInstance.getServerState()) {
            case RUNNING:
                if(isDebug()) {
                    badge = ImageUtilities.loadImage(DEBUGGING_ICON);
                } else if (isProfile()) {
                    badge = ImageUtilities.loadImage(PROFILING_ICON);
                } else {
                    badge = ImageUtilities.loadImage(RUNNING_ICON);
                }
                break;
//            case RUNNING_JVM_DEBUG:
//                badge = ImageUtilities.loadImage(DEBUGGING_ICON);
//                break;
            case STARTING:
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            case STOPPED:
//                badge = ImageUtilities.loadImage(SUSPENDED_ICON);
                break;
            case STOPPED_JVM_BP:
            case STOPPED_JVM_PROFILER:
                badge = ImageUtilities.loadImage(SUSPENDED_ICON);
                break;
            case STOPPING:
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            // TODO profiler states
//            case PROFILING: 
//                badge = ImageUtilities.loadImage(PROFILING_ICON);
//                break;
//            case PROFILER_BLOCKING: 
//                badge = ImageUtilities.loadImage(PROFILER_BLOCKING_ICON);
//                break;
//            case PROFILER_STARTING: 
//                badge = ImageUtilities.loadImage(WAITING_ICON);
//                break;
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }

    private boolean isDebug() {
        return GlassfishModule.DEBUG_MODE.equals(
                serverInstance.getProperty(GlassfishModule.JVM_MODE));
    }
    
    private boolean isProfile() {
        return GlassfishModule.PROFILE_MODE.equals(
                serverInstance.getProperty(GlassfishModule.JVM_MODE));
    }

/*    private Map<String, String> getInstanceProperties() {
        Map<String, String> ip = serverInstance.getProperties();
//        GlassfishModule commonSupport = getLookup().lookup(GlassfishModule.class);
        if(ip == null) {
            ip = Collections.emptyMap();
        }
        return ip;
    }
*/    
    private String buildDisplayName() {
        String dn = serverInstance.getProperty(GlassfishModule.DISPLAY_NAME_ATTR);
        return dn != null ? dn : "Bogus display name"; // NOI18N NbBundle.getMessage(Hk2InstanceNode.class, "TXT_GlassfishPreludeInstanceNode");
    }

    private String getAdminUrl() {
        String result = null;
        String host = serverInstance.getProperty(GlassfishModule.HOSTNAME_ATTR);
        String adminPort = !"false".equals(System.getProperty("glassfish.useadminport"))
                ? serverInstance.getProperty(GlassfishModule.ADMINPORT_ATTR)
                : serverInstance.getProperty(GlassfishModule.HTTPPORT_ATTR);
        if(host != null && host.length() > 0) {
            result = "http://" + host + ":" + adminPort; // this is just a display string...
        }
        
        return result;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                fireIconChange();
            }
        });
    }

}
