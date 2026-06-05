# XR_11 - VR Scene Builder Design Document

## Document Metadata

| Field | Value |
|-------|-------|
| Document ID | XR_11 |
| Title | VR Scene Builder - No-Code Visual Editor |
| Version | 1.0 |
| Status | Draft |
| Platform | XRVista - Enterprise XR SaaS Platform |
| Tech Stack | React, Redux, ThreeJS, AFrame, TypeScript |
| Estimated Effort | 5 sprints |

---

## 1. Overview

### 1.1 Purpose

The VR Scene Builder is a no-code, drag-and-drop visual editor that enables enterprise users to create, edit, and publish immersive VR/AR/XR experiences without writing any code. Users can compose 360-degree tours, interactive hotspots, 3D model placements, multimedia overlays, and publish them to multiple platforms (Web, Mobile, Cardboard, Meta Quest, Apple Vision Pro).

### 1.2 Key Capabilities

| Capability | Description |
|-----------|-------------|
| Drag & Drop Editor | Visual node-based scene composition |
| Real-time Preview | WebGL viewport with instant feedback |
| Multi-format Export | WebXR, Mobile App, Cardboard, Quest, Vision Pro |
| Collaboration | Real-time multi-user editing |
| Template Library | Pre-built scene templates |
| AI Assistance | AI-powered scene generation from prompts |
| Asset Management | Unified asset library with CDN delivery |
| Version Control | Scene versioning and rollback |

### 1.3 User Roles

| Role | Permissions |
|------|------------|
| Scene Author | Create, edit, publish own scenes |
| Scene Reviewer | Review, comment, approve scenes |
| Scene Publisher | Publish scenes to production |
| Admin | Manage templates, assets, users |

---

## 2. Architecture

### 2.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         SCENE BUILDER UI                                  │
│                         (React + TypeScript)                              │
├──────────┬────────────────────────────────────────┬──────────────────────┤
│          │                                        │                      │
│ ASSET    │           3D VIEWPORT                  │   PROPERTIES         │
│ PANEL    │           (ThreeJS Canvas)             │   PANEL              │
│          │                                        │                      │
│ ┌──────┐ │  ┌──────────────────────────────────┐  │  ┌────────────────┐ │
│ │Browse│ │  │                                  │  │  │ Transform      │ │
│ │Search│ │  │     WebGL Canvas                 │  │  │ - Position XYZ │ │
│ │Filter│ │  │     (ThreeJS r160+)              │  │  │ - Rotation XYZ │ │
│ │      │ │  │                                  │  │  │ - Scale XYZ    │ │
│ │ ──── │ │  │  ┌────────────────────────────┐ │  │  ├────────────────┤ │
│ │Models│ │  │  │  Scene Preview              │ │  │  │ Material       │ │
│ │ -glTF│ │  │  │                            │ │  │  │ - Color        │ │
│ │ -FBX │ │  │  │  [Interactive 3D Scene]    │ │  │  │ - Texture      │ │
│ │ -OBJ │ │  │  │                            │ │  │  │ - Opacity      │ │
│ │      │ │  │  │  - 360 Background          │ │  │  │ - Metalness    │ │
│ │ ──── │ │  │  │  - Hotspots                │ │  │  ├────────────────┤ │
│ │Videos│ │  │  │  - 3D Models               │ │  │  │ Animation      │ │
│ │ -MP4 │ │  │  │  - UI Panels               │ │  │  │ - Play/Pause   │ │
│ │ -WebM│ │  │  │  - Audio Sources           │ │  │  │ - Loop         │ │
│ │      │ │  │  │                            │ │  │  │ - Duration     │ │
│ │ ──── │ │  │  └────────────────────────────┘ │  │  ├────────────────┤ │
│ │Audio │ │  │                                  │  │  │ Interaction    │ │
│ │ -MP3 │ │  │  ┌────────────────────────────┐ │  │  │ - On Click     │ │
│ │ -WAV │ │  │  │  Viewport Controls         │ │  │  │ - On Hover     │ │
│ │      │ │  │  │  - Orbit Controls          │ │  │  │ - Navigate     │ │
│ │ ──── │ │  │  │  - Pan                     │ │  │  │ - Play Media   │ │
│ │Images│ │  │  │  - Zoom                    │ │  │  ├────────────────┤ │
│ │ -PNG │ │  │  │  - Reset Camera            │ │  │  │ Visibility     │ │
│ │ -JPG │ │  │  │  - Toggle Grid             │ │  │  │ - Show/Hide    │ │
│ │ -SVG │ │  │  │  - Snap to Grid            │ │  │  │ - LOD Level    │ │
│ │      │ │  │  └────────────────────────────┘ │  │  │ - VR Only      │ │
│ │ ──── │ │  │                                  │  │  │ - Cardboard    │ │
│ │HDR   │ │  └──────────────────────────────────┘  │  └────────────────┘ │
│ │ -HDR │ │                                        │                      │
│ │ -EXR │ │  TOOLBAR                               │ SCENE TREE           │
│ │      │ │  ┌────────────────────────────────┐    │ ┌────────────────┐ │
│ └──────┘ │  │ 💾 Save │ ↩ Undo │ ↪ Redo     │    │ │ 📁 Root        │ │
│          │  │ 👁 Preview │ 🚀 Publish        │    │ │   ├ 📷 Scene 1 │ │
│          │  │ 📐 Grid │ 🧲 Snap │ 📏 Measure │    │ │   │  ├ 🔘 Hot  │ │
│          │  │ 📱 Device Preview │ 🔗 Share    │    │ │   │  ├ 🧊 Model│ │
│          │  └────────────────────────────────┘    │ │   │  └ 📝 Text │ │
│          │                                        │ │   └ 📷 Scene 2 │ │
│          │                                        │ │     └ 🔘 Hot  │ │
│          │                                        │ └────────────────┘ │
└──────────┴────────────────────────────────────────┴──────────────────────┘
```

### 2.2 Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| UI Framework | React 18 + TypeScript | Component rendering |
| State Management | Redux Toolkit | Application state |
| 3D Rendering | ThreeJS r160 | WebGL viewport |
| VR Framework | AFrame 1.6 | WebXR scene output |
| Drag & Drop | React DnD | Interactive drag/drop |
| Styling | Tailwind CSS + Radix UI | Design system |
| Collaboration | Yjs + WebSocket | CRDT-based sync |
| Build Tool | Vite 5 | Fast bundling |
| Testing | Vitest + Testing Library | Unit/integration tests |

---

## 3. Component Architecture

### 3.1 Component Hierarchy

```
SceneBuilder (Root)
├── TopBar
│   ├── FileMenu (New, Open, Save, Export)
│   ├── EditMenu (Undo, Redo, Copy, Paste, Delete)
│   ├── ViewMenu (Grid, Snap, Preview Mode)
│   └── PublishButton
│
├── Toolbar
│   ├── UndoRedoGroup
│   ├── TransformTools (Select, Move, Rotate, Scale)
│   ├── GridToggle
│   ├── SnapToggle
│   ├── MeasurementTool
│   └── DevicePreviewSelector
│
├── MainContent
│   ├── AssetPanel
│   │   ├── AssetSearch
│   │   ├── AssetFilters (Type, Tags, Date)
│   │   ├── AssetGrid
│   │   │   ├── ModelAssetCard
│   │   │   ├── VideoAssetCard
│   │   │   ├── AudioAssetCard
│   │   │   ├── ImageAssetCard
│   │   │   └── HDRAstCard
│   │   └── AssetUpload
│   │
│   ├── Viewport
│   │   ├── ThreeJSCanvas
│   │   │   ├── Scene
│   │   │   ├── Camera
│   │   │   ├── Lights
│   │   │   ├── Grid
│   │   │   └── Controls (OrbitControls)
│   │   ├── ViewportToolbar
│   │   │   ├── CameraControls
│   │   │   ├── RenderMode (Wireframe, Solid, Textured)
│   │   │   └── FPSCounter
│   │   └── ViewportOverlays
│   │       ├── SelectionOutline
│   │       ├── TransformGizmo
│   │       └── MeasurementOverlay
│   │
│   └── PropertiesPanel
│       ├── TransformSection
│       ├── MaterialSection
│       ├── AnimationSection
│       ├── InteractionSection
│       └── VisibilitySection
│
├── SceneTreePanel
│   ├── SceneTreeHeader (Add Scene, Collapse All)
│   ├── SceneTreeNode (Recursive)
│   │   ├── NodeIcon
│   │   ├── NodeLabel (Editable)
│   │   ├── VisibilityToggle
│   │   ├── LockToggle
│   │   └── ContextMenu (Delete, Duplicate, Group)
│   └── SceneTreeFooter (Scene Count)
│
├── PreviewModal
│   ├── DeviceFrame (Desktop, Mobile, VR, Cardboard)
│   ├── PreviewCanvas
│   └── PreviewControls (Play, Pause, Reset)
│
└── PublishDialog
    ├── PublishSettings (Version, Changelog)
    ├── PlatformSelector (Web, Mobile, Quest, Vision)
    ├── QRCodeGenerator
    └── PublishButton
```

### 3.2 Component Implementation

```typescript
// SceneBuilder.tsx - Root Component
import React, { useCallback, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';

import { TopBar } from './TopBar';
import { Toolbar } from './Toolbar';
import { AssetPanel } from './AssetPanel';
import { Viewport } from './Viewport';
import { PropertiesPanel } from './PropertiesPanel';
import { SceneTreePanel } from './SceneTreePanel';
import { PreviewModal } from './PreviewModal';
import { PublishDialog } from './PublishDialog';

import {
  selectScene,
  selectSelectedNodeId,
  selectIsDirty,
  selectIsPreviewOpen,
  selectIsPublishDialogOpen,
} from './store/selectors';
import { saveScene, undo, redo } from './store/sceneSlice';
import { useKeyboardShortcuts } from './hooks/useKeyboardShortcuts';

export const SceneBuilder: React.FC = () => {
  const dispatch = useDispatch();
  const scene = useSelector(selectScene);
  const selectedNodeId = useSelector(selectSelectedNodeId);
  const isDirty = useSelector(selectIsDirty);
  const isPreviewOpen = useSelector(selectIsPreviewOpen);
  const isPublishDialogOpen = useSelector(selectIsPublishDialogOpen);

  // Keyboard shortcuts
  useKeyboardShortcuts({
    'ctrl+s': () => dispatch(saveScene()),
    'ctrl+z': () => dispatch(undo()),
    'ctrl+shift+z': () => dispatch(redo()),
  });

  // Auto-save every 30 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      if (isDirty) {
        dispatch(saveScene());
      }
    }, 30000);
    return () => clearInterval(interval);
  }, [isDirty, dispatch]);

  const handleDrop = useCallback((item: DragItem, position: Vector3) => {
    dispatch(addNodeFromAsset({ asset: item.asset, position }));
  }, [dispatch]);

  return (
    <DndProvider backend={HTML5Backend}>
      <div className="scene-builder flex flex-col h-screen bg-gray-900 text-white">
        <TopBar />
        <Toolbar />
        
        <div className="flex flex-1 overflow-hidden">
          <AssetPanel />
          
          <main className="flex-1 relative">
            <Viewport onDrop={handleDrop} />
          </main>
          
          <div className="w-80 border-l border-gray-700 flex flex-col">
            <PropertiesPanel />
            <SceneTreePanel />
          </div>
        </div>
        
        {isPreviewOpen && <PreviewModal />}
        {isPublishDialogOpen && <PublishDialog />}
      </div>
    </DndProvider>
  );
};
```

```typescript
// Viewport.tsx - 3D Viewport Component
import React, { useRef, useEffect, useCallback } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { useDrop } from 'react-dnd';
import { useSelector, useDispatch } from 'react-redux';

import { selectScene, selectSelectedNodeId, selectGridVisible } from './store/selectors';
import { setSelectedNode, updateNodeTransform } from './store/sceneSlice';
import { GridHelper } from './three/GridHelper';
import { SelectionOutline } from './three/SelectionOutline';
import { TransformGizmo } from './three/TransformGizmo';

interface ViewportProps {
  onDrop: (item: DragItem, position: Vector3) => void;
}

export const Viewport: React.FC<ViewportProps> = ({ onDrop }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const sceneRef = useRef<THREE.Scene | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const cameraRef = useRef<THREE.PerspectiveCamera | null>(null);
  const controlsRef = useRef<OrbitControls | null>(null);
  
  const scene = useSelector(selectScene);
  const selectedNodeId = useSelector(selectSelectedNodeId);
  const gridVisible = useSelector(selectGridVisible);
  const dispatch = useDispatch();

  // Drop target for asset panel
  const [{ isOver }, drop] = useDrop(() => ({
    accept: 'ASSET',
    drop: (item: DragItem, monitor) => {
      const offset = monitor.getClientOffset();
      if (offset && canvasRef.current) {
        const position = screenToWorld(offset.x, offset.y);
        onDrop(item, position);
      }
    },
    collect: (monitor) => ({
      isOver: monitor.isOver(),
    }),
  }), [onDrop]);

  // Initialize ThreeJS scene
  useEffect(() => {
    if (!canvasRef.current) return;

    const scene = new THREE.Scene();
    sceneRef.current = scene;

    const camera = new THREE.PerspectiveCamera(
      75,
      canvasRef.current.clientWidth / canvasRef.current.clientHeight,
      0.1,
      1000
    );
    camera.position.set(0, 2, 5);
    cameraRef.current = camera;

    const renderer = new THREE.WebGLRenderer({
      canvas: canvasRef.current,
      antialias: true,
      alpha: true,
    });
    renderer.setSize(
      canvasRef.current.clientWidth,
      canvasRef.current.clientHeight
    );
    renderer.setPixelRatio(window.devicePixelRatio);
    rendererRef.current = renderer;

    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controlsRef.current = controls;

    // Add grid
    if (gridVisible) {
      const grid = new GridHelper(50, 50, 0x444444, 0x222222);
      scene.add(grid);
    }

    // Add lights
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(5, 10, 7.5);
    scene.add(directionalLight);

    // Animation loop
    let animationId: number;
    const animate = () => {
      animationId = requestAnimationFrame(animate);
      controls.update();
      renderer.render(scene, camera);
    };
    animate();

    // Handle resize
    const handleResize = () => {
      if (!canvasRef.current) return;
      const width = canvasRef.current.clientWidth;
      const height = canvasRef.current.clientHeight;
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.setSize(width, height);
    };
    window.addEventListener('resize', handleResize);

    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener('resize', handleResize);
      renderer.dispose();
      controls.dispose();
    };
  }, []);

  // Update scene when scene data changes
  useEffect(() => {
    if (!sceneRef.current) return;
    syncSceneGraph(sceneRef.current, scene);
  }, [scene]);

  // Handle click for selection
  const handleClick = useCallback((event: React.MouseEvent) => {
    if (!canvasRef.current || !cameraRef.current || !sceneRef.current) return;

    const rect = canvasRef.current.getBoundingClientRect();
    const mouse = new THREE.Vector2(
      ((event.clientX - rect.left) / rect.width) * 2 - 1,
      -((event.clientY - rect.top) / rect.height) * 2 + 1
    );

    const raycaster = new THREE.Raycaster();
    raycaster.setFromCamera(mouse, cameraRef.current);

    const intersects = raycaster.intersectObjects(
      sceneRef.current.children,
      true
    );

    if (intersects.length > 0) {
      const selectedObject = intersects[0].object;
      const nodeId = selectedObject.userData.nodeId;
      if (nodeId) {
        dispatch(setSelectedNode(nodeId));
      }
    } else {
      dispatch(setSelectedNode(null));
    }
  }, [dispatch]);

  return (
    <div
      ref={drop}
      className={`w-full h-full relative ${isOver ? 'ring-2 ring-blue-500' : ''}`}
    >
      <canvas
        ref={canvasRef}
        className="w-full h-full"
        onClick={handleClick}
      />
      
      {/* Viewport overlays */}
      <div className="absolute top-4 left-4 flex gap-2">
        <button className="px-3 py-1 bg-gray-800 rounded text-sm">
          Orbit
        </button>
        <button className="px-3 py-1 bg-gray-800 rounded text-sm">
          Pan
        </button>
        <button className="px-3 py-1 bg-gray-800 rounded text-sm">
          Zoom
        </button>
      </div>
      
      <div className="absolute bottom-4 right-4 text-sm text-gray-500">
        {scene.nodes.length} nodes | 60 FPS
      </div>
    </div>
  );
};
```

---

## 4. State Management (Redux)

### 4.1 State Shape

```typescript
// store/types.ts
export interface SceneBuilderState {
  scene: Scene;
  selectedNodeId: string | null;
  hoveredNodeId: string | null;
  history: HistoryStack;
  viewport: ViewportState;
  assets: AssetState;
  ui: UIState;
  collaboration: CollaborationState;
  isDirty: boolean;
  isSaving: boolean;
  isPublishing: boolean;
}

export interface Scene {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  version: number;
  nodes: SceneNode[];
  settings: SceneSettings;
}

export interface SceneNode {
  id: string;
  type: NodeType;
  name: string;
  visible: boolean;
  locked: boolean;
  parentId: string | null;
  children: string[];
  transform: Transform;
  properties: NodeProperties;
  interactions: Interaction[];
  metadata: Record<string, any>;
}

export type NodeType =
  | 'scene'
  | 'background'
  | 'hotspot_info'
  | 'hotspot_nav'
  | 'hotspot_media'
  | 'text'
  | 'image'
  | 'video'
  | 'audio'
  | 'model_3d'
  | 'ui_panel'
  | 'light'
  | 'group';

export interface Transform {
  position: Vector3;
  rotation: Vector3;
  scale: Vector3;
}

export interface Vector3 {
  x: number;
  y: number;
  z: number;
}

export interface NodeProperties {
  // Hotspot
  title?: string;
  description?: string;
  icon?: string;
  targetSceneId?: string;
  transition?: TransitionType;
  
  // Media
  assetId?: string;
  mediaUrl?: string;
  autoplay?: boolean;
  loop?: boolean;
  volume?: number;
  
  // Text
  content?: string;
  fontSize?: number;
  color?: string;
  font?: string;
  
  // Image
  width?: number;
  height?: number;
  opacity?: number;
  
  // 3D Model
  modelUrl?: string;
  
  // UI Panel
  background?: string;
  borderRadius?: number;
  
  // Light
  lightType?: 'ambient' | 'directional' | 'point' | 'spot';
  lightColor?: string;
  intensity?: number;
  
  // Material
  materialColor?: string;
  textureUrl?: string;
  metalness?: number;
  roughness?: number;
}

export interface Interaction {
  id: string;
  trigger: InteractionTrigger;
  action: InteractionAction;
  parameters: Record<string, any>;
}

export type InteractionTrigger = 'click' | 'hover' | 'gaze' | 'enter_scene';

export type InteractionAction =
  | 'navigate'
  | 'play_media'
  | 'show_panel'
  | 'hide_panel'
  | 'toggle_visibility'
  | 'animate';

export interface HistoryStack {
  past: SceneSnapshot[];
  future: SceneSnapshot[];
  maxSize: number;
}

export interface SceneSnapshot {
  scene: Scene;
  timestamp: number;
  action: string;
}

export interface ViewportState {
  camera: {
    position: Vector3;
    target: Vector3;
    zoom: number;
  };
  gridVisible: boolean;
  snapEnabled: boolean;
  snapSize: number;
  renderMode: 'wireframe' | 'solid' | 'textured';
  devicePreview: 'desktop' | 'mobile' | 'vr' | 'cardboard';
}

export interface AssetState {
  items: Asset[];
  categories: AssetCategory[];
  loading: boolean;
  error: string | null;
  uploadProgress: number;
}

export interface Asset {
  id: string;
  name: string;
  type: AssetType;
  url: string;
  thumbnailUrl: string;
  size: number;
  tags: string[];
  createdAt: string;
}

export type AssetType = 'model' | 'video' | 'audio' | 'image' | 'hdr';

export interface UIState {
  isPreviewOpen: boolean;
  isPublishDialogOpen: boolean;
  isAssetPanelCollapsed: boolean;
  isPropertiesPanelCollapsed: boolean;
  isSceneTreeCollapsed: boolean;
  activeTab: 'assets' | 'templates' | 'ai';
}

export interface CollaborationState {
  connected: boolean;
  users: Collaborator[];
  cursors: Map<string, CursorPosition>;
  locks: Map<string, string>;
}

export interface Collaborator {
  id: string;
  name: string;
  color: string;
  avatar: string;
}

export interface CursorPosition {
  x: number;
  y: number;
  nodeId: string | null;
}
```

### 4.2 Redux Slice

```typescript
// store/sceneSlice.ts
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Scene, SceneNode, Vector3, HistoryStack } from './types';
import { generateId } from '../utils/id';
import { deepClone } from '../utils/clone';

interface SceneState {
  scene: Scene;
  selectedNodeId: string | null;
  hoveredNodeId: string | null;
  history: HistoryStack;
  isDirty: boolean;
  isSaving: boolean;
  isPublishing: boolean;
}

const initialState: SceneState = {
  scene: {
    id: '',
    name: 'Untitled Scene',
    description: '',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    version: 1,
    nodes: [
      {
        id: 'root',
        type: 'scene',
        name: 'Root',
        visible: true,
        locked: false,
        parentId: null,
        children: [],
        transform: {
          position: { x: 0, y: 0, z: 0 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
        properties: {},
        interactions: [],
        metadata: {},
      },
    ],
    settings: {
      backgroundColor: '#000000',
      ambientLight: 0.6,
      backgroundType: 'color',
    },
  },
  selectedNodeId: null,
  hoveredNodeId: null,
  history: {
    past: [],
    future: [],
    maxSize: 50,
  },
  isDirty: false,
  isSaving: false,
  isPublishing: false,
};

const sceneSlice = createSlice({
  name: 'scene',
  initialState,
  reducers: {
    // Scene actions
    setScene: (state, action: PayloadAction<Scene>) => {
      state.scene = action.payload;
      state.isDirty = false;
      state.history = { past: [], future: [], maxSize: 50 };
    },

    updateSceneSettings: (state, action: PayloadAction<Partial<Scene['settings']>>) => {
      pushToHistory(state);
      state.scene.settings = { ...state.scene.settings, ...action.payload };
      state.isDirty = true;
    },

    // Node actions
    addNode: (state, action: PayloadAction<{ node: SceneNode; parentId?: string }>) => {
      pushToHistory(state);
      const { node, parentId = 'root' } = action.payload;
      
      state.scene.nodes.push(node);
      
      const parent = state.scene.nodes.find(n => n.id === parentId);
      if (parent) {
        parent.children.push(node.id);
      }
      
      state.isDirty = true;
    },

    removeNode: (state, action: PayloadAction<string>) => {
      pushToHistory(state);
      const nodeId = action.payload;
      
      // Remove from parent's children
      state.scene.nodes.forEach(node => {
        node.children = node.children.filter(id => id !== nodeId);
      });
      
      // Remove node and all descendants
      const removeRecursive = (id: string) => {
        const node = state.scene.nodes.find(n => n.id === id);
        if (node) {
          node.children.forEach(removeRecursive);
          state.scene.nodes = state.scene.nodes.filter(n => n.id !== id);
        }
      };
      removeRecursive(nodeId);
      
      if (state.selectedNodeId === nodeId) {
        state.selectedNodeId = null;
      }
      
      state.isDirty = true;
    },

    updateNodeTransform: (
      state,
      action: PayloadAction<{ nodeId: string; transform: Partial<SceneNode['transform']> }>
    ) => {
      pushToHistory(state);
      const { nodeId, transform } = action.payload;
      
      const node = state.scene.nodes.find(n => n.id === nodeId);
      if (node) {
        node.transform = { ...node.transform, ...transform };
        state.isDirty = true;
      }
    },

    updateNodeProperties: (
      state,
      action: PayloadAction<{ nodeId: string; properties: Partial<SceneNode['properties']> }>
    ) => {
      pushToHistory(state);
      const { nodeId, properties } = action.payload;
      
      const node = state.scene.nodes.find(n => n.id === nodeId);
      if (node) {
        node.properties = { ...node.properties, ...properties };
        state.isDirty = true;
      }
    },

    setNodeVisible: (state, action: PayloadAction<{ nodeId: string; visible: boolean }>) => {
      const { nodeId, visible } = action.payload;
      const node = state.scene.nodes.find(n => n.id === nodeId);
      if (node) {
        node.visible = visible;
        state.isDirty = true;
      }
    },

    setNodeLocked: (state, action: PayloadAction<{ nodeId: string; locked: boolean }>) => {
      const { nodeId, locked } = action.payload;
      const node = state.scene.nodes.find(n => n.id === nodeId);
      if (node) {
        node.locked = locked;
      }
    },

    reorderNode: (
      state,
      action: PayloadAction<{ nodeId: string; newIndex: number }>
    ) => {
      pushToHistory(state);
      const { nodeId, newIndex } = action.payload;
      
      const node = state.scene.nodes.find(n => n.id === nodeId);
      if (node && node.parentId) {
        const parent = state.scene.nodes.find(n => n.id === node.parentId);
        if (parent) {
          const oldIndex = parent.children.indexOf(nodeId);
          parent.children.splice(oldIndex, 1);
          parent.children.splice(newIndex, 0, nodeId);
          state.isDirty = true;
        }
      }
    },

    duplicateNode: (state, action: PayloadAction<string>) => {
      pushToHistory(state);
      const nodeId = action.payload;
      
      const original = state.scene.nodes.find(n => n.id === nodeId);
      if (original) {
        const duplicate: SceneNode = {
          ...deepClone(original),
          id: generateId(),
          name: `${original.name} (Copy)`,
          transform: {
            ...original.transform,
            position: {
              x: original.transform.position.x + 0.5,
              y: original.transform.position.y,
              z: original.transform.position.z,
            },
          },
        };
        
        state.scene.nodes.push(duplicate);
        
        const parent = state.scene.nodes.find(n => n.id === original.parentId);
        if (parent) {
          const index = parent.children.indexOf(nodeId);
          parent.children.splice(index + 1, 0, duplicate.id);
        }
        
        state.isDirty = true;
      }
    },

    // Selection
    setSelectedNode: (state, action: PayloadAction<string | null>) => {
      state.selectedNodeId = action.payload;
    },

    setHoveredNode: (state, action: PayloadAction<string | null>) => {
      state.hoveredNodeId = action.payload;
    },

    // History
    undo: (state) => {
      if (state.history.past.length === 0) return;
      
      const previous = state.history.past.pop()!;
      state.history.future.push(deepClone(state.scene));
      state.scene = previous.scene;
      state.isDirty = true;
    },

    redo: (state) => {
      if (state.history.future.length === 0) return;
      
      const next = state.history.future.pop()!;
      state.history.past.push(deepClone(state.scene));
      state.scene = next.scene;
      state.isDirty = true;
    },

    // Save state
    setSaving: (state, action: PayloadAction<boolean>) => {
      state.isSaving = action.payload;
    },

    setSaved: (state) => {
      state.isDirty = false;
      state.isSaving = false;
      state.scene.updatedAt = new Date().toISOString();
    },

    setPublishing: (state, action: PayloadAction<boolean>) => {
      state.isPublishing = action.payload;
    },
  },
});

function pushToHistory(state: SceneState) {
  state.history.past.push(deepClone(state.scene));
  if (state.history.past.length > state.history.maxSize) {
    state.history.past.shift();
  }
  state.history.future = [];
}

export const {
  setScene,
  updateSceneSettings,
  addNode,
  removeNode,
  updateNodeTransform,
  updateNodeProperties,
  setNodeVisible,
  setNodeLocked,
  reorderNode,
  duplicateNode,
  setSelectedNode,
  setHoveredNode,
  undo,
  redo,
  setSaving,
  setSaved,
  setPublishing,
} = sceneSlice.actions;

export default sceneSlice.reducer;
```

### 4.3 Selectors

```typescript
// store/selectors.ts
import { createSelector } from '@reduxjs/toolkit';
import { RootState } from './store';

export const selectSceneState = (state: RootState) => state.scene;

export const selectScene = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.scene
);

export const selectSceneNodes = createSelector(
  [selectScene],
  (scene) => scene.nodes
);

export const selectSelectedNodeId = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.selectedNodeId
);

export const selectSelectedNode = createSelector(
  [selectSceneNodes, selectSelectedNodeId],
  (nodes, selectedId) => {
    if (!selectedId) return null;
    return nodes.find(n => n.id === selectedId) ?? null;
  }
);

export const selectRootNodes = createSelector(
  [selectSceneNodes],
  (nodes) => {
    const root = nodes.find(n => n.id === 'root');
    if (!root) return [];
    return root.children
      .map(id => nodes.find(n => n.id === id))
      .filter(Boolean);
  }
);

export const selectChildNodes = createSelector(
  [selectSceneNodes, (_, parentId: string) => parentId],
  (nodes, parentId) => {
    const parent = nodes.find(n => n.id === parentId);
    if (!parent) return [];
    return parent.children
      .map(id => nodes.find(n => n.id === id))
      .filter(Boolean);
  }
);

export const selectIsDirty = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.isDirty
);

export const selectHistory = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.history
);

export const selectCanUndo = createSelector(
  [selectHistory],
  (history) => history.past.length > 0
);

export const selectCanRedo = createSelector(
  [selectHistory],
  (history) => history.future.length > 0
);

export const selectViewport = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.viewport
);

export const selectGridVisible = createSelector(
  [selectViewport],
  (viewport) => viewport.gridVisible
);

export const selectIsPreviewOpen = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.ui.isPreviewOpen
);

export const selectIsPublishDialogOpen = createSelector(
  [selectSceneState],
  (sceneState) => sceneState.ui.isPublishDialogOpen
);
```

---

## 5. Drag & Drop System

### 5.1 Drag & Drop Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   DRAG & DROP SYSTEM                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  SOURCE: Asset Panel                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Model3D Card                                        │    │
│  │  ┌─────────┐                                        │    │
│  │  │  [glTF] │  ──── drag ─────▶                     │    │
│  │  │  Model  │                                        │    │
│  │  └─────────┘                                        │    │
│  │  data: { type: 'ASSET', asset: {...} }              │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  TARGET: Viewport                                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  WebGL Canvas                                       │    │
│  │  ┌───────────────────────────────────────────────┐  │    │
│  │  │                                               │  │    │
│  │  │   Drop Zone (entire canvas)                   │  │    │
│  │  │                                               │  │    │
│  │  │   - Convert screen coords → world coords     │  │    │
│  │  │   - Create new node at position              │  │    │
│  │  │   - Select newly created node                │  │    │
│  │  │                                               │  │    │
│  │  └───────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  TARGET: Scene Tree                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  📁 Root                                            │    │
│  │    ├── 📷 Scene 1                                   │    │
│  │    │   └── 🔘 Hotspot  [drop here to reorder]      │    │
│  │    └── 📷 Scene 2                                   │    │
│  │        └── 🧊 Model   [drop here to reparent]      │    │
│  │                                                     │    │
│  │  Drop between nodes = reorder                       │    │
│  │  Drop on node = reparent                            │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  TRANSFORM: Viewport                                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Selected Node                                      │    │
│  │  ┌───────────────────────────────────────────────┐  │    │
│  │  │  [Gizmo] ──── drag ─────▶ Update position    │  │    │
│  │  │   ↕                                            │  │    │
│  │  │   ↔                                            │  │    │
│  │  │                                                │  │    │
│  │  │  - Constrain to axis (Shift + drag)           │  │    │
│  │  │  - Snap to grid (if enabled)                  │  │    │
│  │  │  - Update transform in Redux                  │  │    │
│  │  └───────────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Drag & Drop Implementation

```typescript
// components/AssetCard/ModelAssetCard.tsx
import React from 'react';
import { useDrag } from 'react-dnd';
import { Asset } from '../../store/types';

interface ModelAssetCardProps {
  asset: Asset;
}

export const ModelAssetCard: React.FC<ModelAssetCardProps> = ({ asset }) => {
  const [{ isDragging }, drag] = useDrag(() => ({
    type: 'ASSET',
    item: { 
      type: 'ASSET', 
      asset: {
        ...asset,
        nodeType: 'model_3d',
      },
    },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  }), [asset]);

  return (
    <div
      ref={drag}
      className={`
        asset-card p-2 rounded-lg cursor-move border border-gray-700
        hover:border-blue-500 transition-colors
        ${isDragging ? 'opacity-50' : 'opacity-100'}
      `}
    >
      <div className="aspect-square bg-gray-800 rounded flex items-center justify-center mb-2">
        <img 
          src={asset.thumbnailUrl} 
          alt={asset.name}
          className="max-w-full max-h-full object-contain"
        />
      </div>
      <p className="text-sm truncate">{asset.name}</p>
      <p className="text-xs text-gray-500">{formatSize(asset.size)}</p>
    </div>
  );
};

// components/Viewport/ViewportDropZone.tsx
import React, { useCallback } from 'react';
import { useDrop } from 'react-dnd';
import { useDispatch, useSelector } from 'react-redux';
import { addNode } from '../../store/sceneSlice';
import { selectScene } from '../../store/selectors';
import { SceneNode, Vector3 } from '../../store/types';
import { generateId } from '../../utils/id';

export const ViewportDropZone: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const dispatch = useDispatch();
  const scene = useSelector(selectScene);

  const handleDrop = useCallback((item: any, monitor: DropTargetMonitor) => {
    const clientOffset = monitor.getClientOffset();
    if (!clientOffset) return;

    // Convert screen coordinates to 3D world position
    const worldPosition = screenToWorldPosition(clientOffset.x, clientOffset.y);

    if (item.type === 'ASSET') {
      const newNode: SceneNode = {
        id: generateId(),
        type: item.asset.nodeType,
        name: item.asset.name,
        visible: true,
        locked: false,
        parentId: 'root',
        children: [],
        transform: {
          position: worldPosition,
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
        properties: {
          assetId: item.asset.id,
          modelUrl: item.asset.url,
        },
        interactions: [],
        metadata: {},
      };

      dispatch(addNode({ node: newNode }));
    }
  }, [dispatch]);

  const [{ isOver, canDrop }, drop] = useDrop(() => ({
    accept: 'ASSET',
    drop: handleDrop,
    collect: (monitor) => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
    }),
  }), [handleDrop]);

  return (
    <div
      ref={drop}
      className={`
        w-full h-full relative
        ${isOver ? 'ring-2 ring-blue-500 ring-inset' : ''}
        ${canDrop && !isOver ? 'ring-1 ring-blue-300 ring-inset' : ''}
      `}
    >
      {children}
    </div>
  );
};
```

---

## 6. Node Types & Properties

### 6.1 Node Type Registry

```typescript
// registry/nodeRegistry.ts
import { NodeType, NodeProperties } from '../store/types';

export interface NodeTypeDefinition {
  type: NodeType;
  name: string;
  icon: string;
  color: string;
  category: 'basic' | 'media' | 'interactive' | 'layout' | 'lighting';
  defaultProperties: Partial<NodeProperties>;
  propertySchema: PropertySchema[];
}

export interface PropertySchema {
  key: string;
  label: string;
  type: 'string' | 'number' | 'boolean' | 'color' | 'select' | 'vector3' | 'asset';
  options?: { label: string; value: any }[];
  min?: number;
  max?: number;
  step?: number;
  default?: any;
}

export const NODE_TYPES: Record<NodeType, NodeTypeDefinition> = {
  scene: {
    type: 'scene',
    name: 'Scene',
    icon: '🎬',
    color: '#6366f1',
    category: 'basic',
    defaultProperties: {},
    propertySchema: [],
  },
  background: {
    type: 'background',
    name: 'Background',
    icon: '🖼️',
    color: '#8b5cf6',
    category: 'basic',
    defaultProperties: {
      backgroundType: '360_video',
    },
    propertySchema: [
      { key: 'backgroundType', label: 'Type', type: 'select', 
        options: [
          { label: 'Color', value: 'color' },
          { label: '360 Image', value: '360_image' },
          { label: '360 Video', value: '360_video' },
          { label: 'Gradient', value: 'gradient' },
        ],
      },
      { key: 'assetId', label: 'Asset', type: 'asset' },
      { key: 'materialColor', label: 'Color', type: 'color', default: '#000000' },
    ],
  },
  hotspot_info: {
    type: 'hotspot_info',
    name: 'Info Hotspot',
    icon: 'ℹ️',
    color: '#06b6d4',
    category: 'interactive',
    defaultProperties: {
      title: 'Hotspot',
      description: 'Click for more info',
      icon: 'info',
    },
    propertySchema: [
      { key: 'title', label: 'Title', type: 'string', default: 'Hotspot' },
      { key: 'description', label: 'Description', type: 'string' },
      { key: 'icon', label: 'Icon', type: 'select',
        options: [
          { label: 'Info', value: 'info' },
          { label: 'Question', value: 'question' },
          { label: 'Warning', value: 'warning' },
          { label: 'Star', value: 'star' },
        ],
      },
    ],
  },
  hotspot_nav: {
    type: 'hotspot_nav',
    name: 'Navigation Hotspot',
    icon: '🔗',
    color: '#10b981',
    category: 'interactive',
    defaultProperties: {
      targetSceneId: '',
      transition: 'fade',
    },
    propertySchema: [
      { key: 'targetSceneId', label: 'Target Scene', type: 'string' },
      { key: 'transition', label: 'Transition', type: 'select',
        options: [
          { label: 'Fade', value: 'fade' },
          { label: 'Slide', value: 'slide' },
          { label: 'Dissolve', value: 'dissolve' },
          { label: 'None', value: 'none' },
        ],
      },
    ],
  },
  hotspot_media: {
    type: 'hotspot_media',
    name: 'Media Hotspot',
    icon: '🎬',
    color: '#f59e0b',
    category: 'interactive',
    defaultProperties: {
      mediaType: 'video',
      autoplay: false,
      loop: false,
      volume: 0.8,
    },
    propertySchema: [
      { key: 'mediaType', label: 'Media Type', type: 'select',
        options: [
          { label: 'Video', value: 'video' },
          { label: 'Image', value: 'image' },
          { label: 'Audio', value: 'audio' },
          { label: '3D Model', value: 'model' },
        ],
      },
      { key: 'assetId', label: 'Asset', type: 'asset' },
      { key: 'autoplay', label: 'Autoplay', type: 'boolean', default: false },
      { key: 'loop', label: 'Loop', type: 'boolean', default: false },
      { key: 'volume', label: 'Volume', type: 'number', min: 0, max: 1, step: 0.1, default: 0.8 },
    ],
  },
  text: {
    type: 'text',
    name: 'Text',
    icon: '📝',
    color: '#ec4899',
    category: 'layout',
    defaultProperties: {
      content: 'Text',
      fontSize: 24,
      color: '#ffffff',
      font: 'Arial',
    },
    propertySchema: [
      { key: 'content', label: 'Content', type: 'string', default: 'Text' },
      { key: 'fontSize', label: 'Font Size', type: 'number', min: 8, max: 200, default: 24 },
      { key: 'color', label: 'Color', type: 'color', default: '#ffffff' },
      { key: 'font', label: 'Font', type: 'select',
        options: [
          { label: 'Arial', value: 'Arial' },
          { label: 'Helvetica', value: 'Helvetica' },
          { label: 'Times New Roman', value: 'Times New Roman' },
          { label: 'Courier New', value: 'Courier New' },
        ],
      },
    ],
  },
  image: {
    type: 'image',
    name: 'Image',
    icon: '🖼️',
    color: '#14b8a6',
    category: 'media',
    defaultProperties: {
      width: 1,
      height: 1,
      opacity: 1,
    },
    propertySchema: [
      { key: 'assetId', label: 'Asset', type: 'asset' },
      { key: 'width', label: 'Width', type: 'number', min: 0.1, max: 100, step: 0.1, default: 1 },
      { key: 'height', label: 'Height', type: 'number', min: 0.1, max: 100, step: 0.1, default: 1 },
      { key: 'opacity', label: 'Opacity', type: 'number', min: 0, max: 1, step: 0.1, default: 1 },
    ],
  },
  video: {
    type: 'video',
    name: 'Video',
    icon: '🎥',
    color: '#a855f7',
    category: 'media',
    defaultProperties: {
      autoplay: false,
      loop: false,
      volume: 0.8,
    },
    propertySchema: [
      { key: 'assetId', label: 'Asset', type: 'asset' },
      { key: 'autoplay', label: 'Autoplay', type: 'boolean', default: false },
      { key: 'loop', label: 'Loop', type: 'boolean', default: false },
      { key: 'volume', label: 'Volume', type: 'number', min: 0, max: 1, step: 0.1, default: 0.8 },
      { key: 'width', label: 'Width', type: 'number', min: 0.1, max: 100, default: 4 },
      { key: 'height', label: 'Height', type: 'number', min: 0.1, max: 100, default: 2.25 },
    ],
  },
  audio: {
    type: 'audio',
    name: 'Audio',
    icon: '🔊',
    color: '#f43f5e',
    category: 'media',
    defaultProperties: {
      loop: false,
      volume: 0.8,
      spatial: true,
    },
    propertySchema: [
      { key: 'assetId', label: 'Asset', type: 'asset' },
      { key: 'loop', label: 'Loop', type: 'boolean', default: false },
      { key: 'volume', label: 'Volume', type: 'number', min: 0, max: 1, step: 0.1, default: 0.8 },
      { key: 'spatial', label: 'Spatial Audio', type: 'boolean', default: true },
      { key: 'refDistance', label: 'Reference Distance', type: 'number', min: 0.1, max: 100, default: 1 },
      { key: 'maxDistance', label: 'Max Distance', type: 'number', min: 1, max: 1000, default: 100 },
    ],
  },
  model_3d: {
    type: 'model_3d',
    name: '3D Model',
    icon: '🧊',
    color: '#3b82f6',
    category: 'media',
    defaultProperties: {},
    propertySchema: [
      { key: 'assetId', label: 'Asset', type: 'asset' },
    ],
  },
  ui_panel: {
    type: 'ui_panel',
    name: 'UI Panel',
    icon: '📋',
    color: '#64748b',
    category: 'layout',
    defaultProperties: {
      width: 2,
      height: 1,
      background: '#00000080',
      borderRadius: 0.1,
    },
    propertySchema: [
      { key: 'width', label: 'Width', type: 'number', min: 0.1, max: 100, default: 2 },
      { key: 'height', label: 'Height', type: 'number', min: 0.1, max: 100, default: 1 },
      { key: 'background', label: 'Background', type: 'color', default: '#00000080' },
      { key: 'borderRadius', label: 'Border Radius', type: 'number', min: 0, max: 1, step: 0.01, default: 0.1 },
      { key: 'content', label: 'Content', type: 'string' },
    ],
  },
  light: {
    type: 'light',
    name: 'Light',
    icon: '💡',
    color: '#fbbf24',
    category: 'lighting',
    defaultProperties: {
      lightType: 'point',
      lightColor: '#ffffff',
      intensity: 1,
    },
    propertySchema: [
      { key: 'lightType', label: 'Type', type: 'select',
        options: [
          { label: 'Ambient', value: 'ambient' },
          { label: 'Directional', value: 'directional' },
          { label: 'Point', value: 'point' },
          { label: 'Spot', value: 'spot' },
        ],
      },
      { key: 'lightColor', label: 'Color', type: 'color', default: '#ffffff' },
      { key: 'intensity', label: 'Intensity', type: 'number', min: 0, max: 10, step: 0.1, default: 1 },
      { key: 'distance', label: 'Distance', type: 'number', min: 0, max: 1000, default: 50 },
      { key: 'angle', label: 'Angle', type: 'number', min: 0, max: 180, default: 60 },
    ],
  },
  group: {
    type: 'group',
    name: 'Group',
    icon: '📁',
    color: '#6b7280',
    category: 'layout',
    defaultProperties: {},
    propertySchema: [],
  },
};
```

---

## 7. Transform Controls

### 7.1 Transform Input Component

```typescript
// components/Properties/TransformSection.tsx
import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { selectSelectedNode } from '../../store/selectors';
import { updateNodeTransform } from '../../store/sceneSlice';
import { Vector3 } from '../../store/types';

export const TransformSection: React.FC = () => {
  const selectedNode = useSelector(selectSelectedNode);
  const dispatch = useDispatch();

  if (!selectedNode) {
    return (
      <div className="p-4 text-gray-500 text-sm">
        No node selected
      </div>
    );
  }

  const handleChange = (
    axis: 'x' | 'y' | 'z',
    value: string,
    property: 'position' | 'rotation' | 'scale'
  ) => {
    const numValue = parseFloat(value);
    if (isNaN(numValue)) return;

    const currentValue = selectedNode.transform[property];
    dispatch(updateNodeTransform({
      nodeId: selectedNode.id,
      transform: {
        [property]: {
          ...currentValue,
          [axis]: numValue,
        },
      },
    }));
  };

  return (
    <div className="p-4 border-b border-gray-700">
      <h3 className="text-sm font-medium text-gray-300 mb-3">Transform</h3>
      
      {/* Position */}
      <Vector3Input
        label="Position"
        value={selectedNode.transform.position}
        onChange={(value) => handleChange(value)}
        property="position"
        step={0.1}
      />
      
      {/* Rotation */}
      <Vector3Input
        label="Rotation"
        value={selectedNode.transform.rotation}
        onChange={(value) => handleChange(value)}
        property="rotation"
        step={1}
        min={-360}
        max={360}
      />
      
      {/* Scale */}
      <Vector3Input
        label="Scale"
        value={selectedNode.transform.scale}
        onChange={(value) => handleChange(value)}
        property="scale"
        step={0.1}
        min={0.01}
        max={100}
      />
    </div>
  );
};

interface Vector3InputProps {
  label: string;
  value: Vector3;
  onChange: (axis: 'x' | 'y' | 'z', value: string) => void;
  property: 'position' | 'rotation' | 'scale';
  step?: number;
  min?: number;
  max?: number;
}

const Vector3Input: React.FC<Vector3InputProps> = ({
  label,
  value,
  onChange,
  property,
  step = 0.1,
  min,
  max,
}) => {
  return (
    <div className="mb-3">
      <label className="text-xs text-gray-400 mb-1 block">{label}</label>
      <div className="grid grid-cols-3 gap-2">
        <NumberInput
          value={value.x}
          onChange={(v) => onChange('x', v)}
          label="X"
          color="text-red-400"
          step={step}
          min={min}
          max={max}
        />
        <NumberInput
          value={value.y}
          onChange={(v) => onChange('y', v)}
          label="Y"
          color="text-green-400"
          step={step}
          min={min}
          max={max}
        />
        <NumberInput
          value={value.z}
          onChange={(v) => onChange('z', v)}
          label="Z"
          color="text-blue-400"
          step={step}
          min={min}
          max={max}
        />
      </div>
    </div>
  );
};

interface NumberInputProps {
  value: number;
  onChange: (value: string) => void;
  label: string;
  color?: string;
  step?: number;
  min?: number;
  max?: number;
}

const NumberInput: React.FC<NumberInputProps> = ({
  value,
  onChange,
  label,
  color = 'text-white',
  step = 0.1,
  min,
  max,
}) => {
  return (
    <div className="relative">
      <span className={`absolute left-2 top-1/2 -translate-y-1/2 text-xs ${color}`}>
        {label}
      </span>
      <input
        type="number"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        step={step}
        min={min}
        max={max}
        className="w-full bg-gray-800 border border-gray-700 rounded px-2 pl-6 py-1 text-sm
                   focus:outline-none focus:border-blue-500 text-white"
      />
    </div>
  );
};
```

### 7.2 Visual Gizmos

```typescript
// three/TransformGizmo.ts
import * as THREE from 'three';

export class TransformGizmo {
  private scene: THREE.Scene;
  private camera: THREE.Camera;
  private renderer: THREE.WebGLRenderer;
  
  private selectedObject: THREE.Object3D | null = null;
  private gizmoGroup: THREE.Group;
  
  private isDragging = false;
  private dragAxis: 'x' | 'y' | 'z' | null = null;
  private dragStartPoint = new THREE.Vector3();
  private dragOffset = new THREE.Vector3();
  
  constructor(
    scene: THREE.Scene,
    camera: THREE.Camera,
    renderer: THREE.WebGLRenderer
  ) {
    this.scene = scene;
    this.camera = camera;
    this.renderer = renderer;
    this.gizmoGroup = new THREE.Group();
    this.gizmoGroup.visible = false;
    this.scene.add(this.gizmoGroup);
    
    this.createGizmo();
  }
  
  private createGizmo() {
    // X axis (red)
    const xArrow = this.createAxis(
      new THREE.Vector3(1, 0, 0),
      0xff0000,
      'x'
    );
    this.gizmoGroup.add(xArrow);
    
    // Y axis (green)
    const yArrow = this.createAxis(
      new THREE.Vector3(0, 1, 0),
      0x00ff00,
      'y'
    );
    this.gizmoGroup.add(yArrow);
    
    // Z axis (blue)
    const zArrow = this.createAxis(
      new THREE.Vector3(0, 0, 1),
      0x0000ff,
      'z'
    );
    this.gizmoGroup.add(zArrow);
    
    // Center sphere
    const sphereGeometry = new THREE.SphereGeometry(0.1, 16, 16);
    const sphereMaterial = new THREE.MeshBasicMaterial({ 
      color: 0xffffff,
      depthTest: false,
    });
    const sphere = new THREE.Mesh(sphereGeometry, sphereMaterial);
    sphere.userData.axis = 'center';
    this.gizmoGroup.add(sphere);
  }
  
  private createAxis(
    direction: THREE.Vector3,
    color: number,
    axis: 'x' | 'y' | 'z'
  ): THREE.Group {
    const group = new THREE.Group();
    
    // Arrow line
    const lineGeometry = new THREE.CylinderGeometry(0.02, 0.02, 1, 8);
    const lineMaterial = new THREE.MeshBasicMaterial({ 
      color,
      depthTest: false,
    });
    const line = new THREE.Mesh(lineGeometry, lineMaterial);
    
    // Rotate line to align with axis
    if (axis === 'x') {
      line.rotation.z = -Math.PI / 2;
    } else if (axis === 'z') {
      line.rotation.x = Math.PI / 2;
    }
    line.position.multiplyScalar(0.5);
    
    // Arrow head
    const coneGeometry = new THREE.ConeGeometry(0.05, 0.15, 8);
    const coneMaterial = new THREE.MeshBasicMaterial({ 
      color,
      depthTest: false,
    });
    const cone = new THREE.Mesh(coneGeometry, coneMaterial);
    
    if (axis === 'x') {
      cone.rotation.z = -Math.PI / 2;
      cone.position.x = 1;
    } else if (axis === 'y') {
      cone.position.y = 1;
    } else {
      cone.rotation.x = Math.PI / 2;
      cone.position.z = 1;
    }
    
    group.add(line);
    group.add(cone);
    
    group.userData.axis = axis;
    
    return group;
  }
  
  attach(object: THREE.Object3D) {
    this.selectedObject = object;
    this.gizmoGroup.visible = true;
    this.updatePosition();
  }
  
  detach() {
    this.selectedObject = null;
    this.gizmoGroup.visible = false;
  }
  
  private updatePosition() {
    if (this.selectedObject) {
      this.gizmoGroup.position.copy(this.selectedObject.position);
    }
  }
  
  onMouseDown(event: MouseEvent) {
    const intersect = this.getIntersect(event);
    if (intersect && intersect.object.userData.axis) {
      this.isDragging = true;
      this.dragAxis = intersect.object.userData.axis;
      this.dragStartPoint.copy(intersect.point);
      
      if (this.selectedObject) {
        this.dragOffset.copy(this.selectedObject.position);
      }
    }
  }
  
  onMouseMove(event: MouseEvent) {
    if (!this.isDragging || !this.dragAxis || !this.selectedObject) return;
    
    const intersect = this.getIntersectOnPlane(event, this.dragAxis);
    if (intersect) {
      const delta = intersect.point.sub(this.dragStartPoint);
      
      // Snap to grid if enabled
      const snapSize = 0.5; // Configurable
      if (snapEnabled) {
        delta.x = Math.round(delta.x / snapSize) * snapSize;
        delta.y = Math.round(delta.y / snapSize) * snapSize;
        delta.z = Math.round(delta.z / snapSize) * snapSize;
      }
      
      // Update position based on drag axis
      const newPosition = this.dragOffset.clone();
      if (this.dragAxis === 'x' || this.dragAxis === 'center') {
        newPosition.x += delta.x;
      }
      if (this.dragAxis === 'y' || this.dragAxis === 'center') {
        newPosition.y += delta.y;
      }
      if (this.dragAxis === 'z' || this.dragAxis === 'center') {
        newPosition.z += delta.z;
      }
      
      this.selectedObject.position.copy(newPosition);
      this.updatePosition();
      
      // Dispatch update to Redux
      this.onTransformChange?.(newPosition);
    }
  }
  
  onMouseUp() {
    this.isDragging = false;
    this.dragAxis = null;
  }
  
  private getIntersect(event: MouseEvent): THREE.Intersection | null {
    const raycaster = new THREE.Raycaster();
    const mouse = new THREE.Vector2();
    
    mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
    
    raycaster.setFromCamera(mouse, this.camera);
    const intersects = raycaster.intersectObjects(this.gizmoGroup.children, true);
    
    return intersects.length > 0 ? intersects[0] : null;
  }
  
  private getIntersectOnPlane(
    event: MouseEvent,
    axis: string
  ): THREE.Intersection | null {
    const raycaster = new THREE.Raycaster();
    const mouse = new THREE.Vector2();
    
    mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
    
    raycaster.setFromCamera(mouse, this.camera);
    
    // Create plane perpendicular to camera for the selected axis
    const plane = new THREE.Plane();
    const cameraDirection = new THREE.Vector3();
    this.camera.getWorldDirection(cameraDirection);
    
    if (axis === 'x') {
      plane.setFromNormalAndCoplanarPoint(
        new THREE.Vector3(0, 0, 1),
        this.gizmoGroup.position
      );
    } else if (axis === 'y') {
      plane.setFromNormalAndCoplanarPoint(
        new THREE.Vector3(0, 0, 1),
        this.gizmoGroup.position
      );
    } else if (axis === 'z') {
      plane.setFromNormalAndCoplanarPoint(
        new THREE.Vector3(0, 1, 0),
        this.gizmoGroup.position
      );
    }
    
    const intersection = new THREE.Vector3();
    raycaster.ray.intersectPlane(plane, intersection);
    
    return intersection ? [{ point: intersection } as THREE.Intersection] : null;
  }
  
  onTransformChange?: (position: THREE.Vector3) => void;
}
```

---

## 8. Undo/Redo Architecture

### 8.1 Command Pattern Implementation

```typescript
// store/history.ts
import { Scene, SceneSnapshot } from './types';
import { deepClone } from '../utils/clone';

export class HistoryManager {
  private past: SceneSnapshot[] = [];
  private future: SceneSnapshot[] = [];
  private maxSize: number;
  
  constructor(maxSize: number = 50) {
    this.maxSize = maxSize;
  }
  
  push(scene: Scene, action: string) {
    const snapshot: SceneSnapshot = {
      scene: deepClone(scene),
      timestamp: Date.now(),
      action,
    };
    
    this.past.push(snapshot);
    
    // Limit history size
    if (this.past.length > this.maxSize) {
      this.past.shift();
    }
    
    // Clear future on new action
    this.future = [];
  }
  
  undo(): Scene | null {
    if (this.past.length === 0) return null;
    
    const snapshot = this.past.pop()!;
    this.future.push(snapshot);
    
    return this.past.length > 0 
      ? this.past[this.past.length - 1].scene 
      : null;
  }
  
  redo(): Scene | null {
    if (this.future.length === 0) return null;
    
    const snapshot = this.future.pop()!;
    this.past.push(snapshot);
    
    return snapshot.scene;
  }
  
  canUndo(): boolean {
    return this.past.length > 0;
  }
  
  canRedo(): boolean {
    return this.future.length > 0;
  }
  
  getUndoAction(): string | null {
    if (this.past.length === 0) return null;
    return this.past[this.past.length - 1].action;
  }
  
  getRedoAction(): string | null {
    if (this.future.length === 0) return null;
    return this.future[this.future.length - 1].action;
  }
  
  clear() {
    this.past = [];
    this.future = [];
  }
  
  getState() {
    return {
      pastCount: this.past.length,
      futureCount: this.future.length,
      lastAction: this.getUndoAction(),
    };
  }
}
```

---

## 9. Scene Preview

### 9.1 Preview Modal

```typescript
// components/Preview/PreviewModal.tsx
import React, { useState, useCallback } from 'react';
import { useSelector } from 'react-redux';
import { selectScene } from '../../store/selectors';

type DeviceMode = 'desktop' | 'mobile' | 'vr' | 'cardboard';
type QualityLevel = 'low' | 'medium' | 'high';

export const PreviewModal: React.FC = () => {
  const scene = useSelector(selectScene);
  const [deviceMode, setDeviceMode] = useState<DeviceMode>('desktop');
  const [quality, setQuality] = useState<QualityLevel>('medium');
  const [isPlaying, setIsPlaying] = useState(false);

  const getDeviceDimensions = (mode: DeviceMode) => {
    switch (mode) {
      case 'desktop':
        return { width: 1920, height: 1080 };
      case 'mobile':
        return { width: 390, height: 844 };
      case 'vr':
        return { width: 2880, height: 1600 };
      case 'cardboard':
        return { width: 1920, height: 1080 };
    }
  };

  const dims = getDeviceDimensions(deviceMode);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <div className="bg-gray-900 rounded-lg overflow-hidden max-w-6xl w-full max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-700">
          <h2 className="text-lg font-medium">Scene Preview</h2>
          
          <div className="flex items-center gap-4">
            {/* Device selector */}
            <div className="flex bg-gray-800 rounded-lg p-1">
              {(['desktop', 'mobile', 'vr', 'cardboard'] as DeviceMode[]).map((mode) => (
                <button
                  key={mode}
                  onClick={() => setDeviceMode(mode)}
                  className={`px-3 py-1 rounded text-sm ${
                    deviceMode === mode
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-400 hover:text-white'
                  }`}
                >
                  {mode.charAt(0).toUpperCase() + mode.slice(1)}
                </button>
              ))}
            </div>
            
            {/* Quality selector */}
            <select
              value={quality}
              onChange={(e) => setQuality(e.target.value as QualityLevel)}
              className="bg-gray-800 border border-gray-700 rounded px-3 py-1 text-sm"
            >
              <option value="low">Low Quality</option>
              <option value="medium">Medium Quality</option>
              <option value="high">High Quality</option>
            </select>
          </div>
        </div>
        
        {/* Preview Canvas */}
        <div className="relative bg-black flex items-center justify-center p-8">
          <div
            className="relative bg-gray-800 rounded overflow-hidden"
            style={{
              width: Math.min(dims.width, window.innerWidth - 200),
              height: Math.min(dims.height, window.innerHeight - 200),
              aspectRatio: `${dims.width}/${dims.height}`,
            }}
          >
            {deviceMode === 'cardboard' ? (
              <div className="flex h-full">
                {/* Left eye */}
                <div className="w-1/2 h-full border-r border-gray-600">
                  <PreviewCanvas
                    scene={scene}
                    quality={quality}
                    eye="left"
                    isPlaying={isPlaying}
                  />
                </div>
                {/* Right eye */}
                <div className="w-1/2 h-full">
                  <PreviewCanvas
                    scene={scene}
                    quality={quality}
                    eye="right"
                    isPlaying={isPlaying}
                  />
                </div>
              </div>
            ) : (
              <PreviewCanvas
                scene={scene}
                quality={quality}
                isPlaying={isPlaying}
              />
            )}
            
            {/* VR overlay indicators */}
            {deviceMode === 'vr' && (
              <div className="absolute inset-0 pointer-events-none">
                <div className="absolute top-4 left-1/2 -translate-x-1/2 text-xs text-gray-400">
                  VR Mode Preview
                </div>
              </div>
            )}
          </div>
        </div>
        
        {/* Controls */}
        <div className="flex items-center justify-center gap-4 p-4 border-t border-gray-700">
          <button
            onClick={() => setIsPlaying(!isPlaying)}
            className="px-4 py-2 bg-blue-600 rounded hover:bg-blue-700"
          >
            {isPlaying ? 'Pause' : 'Play'}
          </button>
          <button
            onClick={() => {/* Reset */}}
            className="px-4 py-2 bg-gray-700 rounded hover:bg-gray-600"
          >
            Reset
          </button>
          <button
            onClick={() => {/* Take screenshot */}}
            className="px-4 py-2 bg-gray-700 rounded hover:bg-gray-600"
          >
            Screenshot
          </button>
        </div>
      </div>
    </div>
  );
};

// components/Preview/PreviewCanvas.tsx
interface PreviewCanvasProps {
  scene: Scene;
  quality: QualityLevel;
  eye?: 'left' | 'right';
  isPlaying: boolean;
}

const PreviewCanvas: React.FC<PreviewCanvasProps> = ({
  scene,
  quality,
  eye,
  isPlaying,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  
  useEffect(() => {
    if (!canvasRef.current) return;
    
    const renderer = new THREE.WebGLRenderer({
      canvas: canvasRef.current,
      antialias: quality !== 'low',
    });
    rendererRef.current = renderer;
    
    // Set quality-specific settings
    switch (quality) {
      case 'low':
        renderer.setPixelRatio(1);
        break;
      case 'medium':
        renderer.setPixelRatio(window.devicePixelRatio);
        break;
      case 'high':
        renderer.setPixelRatio(window.devicePixelRatio * 2);
        break;
    }
    
    return () => {
      renderer.dispose();
    };
  }, [quality]);
  
  useEffect(() => {
    if (!rendererRef.current) return;
    
    const renderLoop = () => {
      if (isPlaying) {
        // Update scene
        rendererRef.current!.render(scene, camera);
        requestAnimationFrame(renderLoop);
      }
    };
    
    renderLoop();
  }, [isPlaying, scene]);
  
  return (
    <canvas
      ref={canvasRef}
      className="w-full h-full"
    />
  );
};
```

---

## 10. Publishing Pipeline

### 10.1 Publish Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    PUBLISHING PIPELINE                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. SCENE VALIDATION                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  ✓ Required fields present                          │    │
│  │  ✓ All assets available on CDN                      │    │
│  │  ✓ No broken references                             │    │
│  │  ✓ Scene graph is valid                             │    │
│  │  ✓ No circular dependencies                        │    │
│  │  ✓ Hotspot targets exist                            │    │
│  └─────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  2. SCENE SERIALIZATION                                      │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Convert scene graph to JSON                     │    │
│  │  - Optimize node structure                         │    │
│  │  - Generate asset manifest                        │    │
│  │  - Create version snapshot                        │    │
│  └─────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  3. ASSET PROCESSING                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Resolve asset URLs                              │    │
│  │  - Upload to CDN (if new)                          │    │
│  │  - Generate thumbnails                            │    │
│  │  - Create LOD variants for 3D models              │    │
│  │  - Compress textures                              │    │
│  └─────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  4. SCENE STORE                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Save to MongoDB (scene-service)                │    │
│  │  - Update version history                         │    │
│  │  - Create backup snapshot                         │    │
│  │  - Generate public URL                           │    │
│  └─────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  5. DEPLOYMENT                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Invalidate CDN cache                          │    │
│  │  - Update scene index                            │    │
│  │  - Generate embed code                          │    │
│  │  - Generate QR code                             │    │
│  │  - Send publish notification                     │    │
│  └─────────────────────────────────────────────────────┘    │
│                          │                                    │
│                          ▼                                    │
│  6. POST-PUBLISH                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  - Analytics tracking enabled                    │    │
│  │  - SEO metadata updated                          │    │
│  │  - Social media preview generated                │    │
│  │  - Collaboration session unlocked                │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 Publish Dialog

```typescript
// components/Publish/PublishDialog.tsx
import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { selectScene, selectIsPublishing } from '../../store/selectors';
import { setPublishing } from '../../store/sceneSlice';
import { publishScene } from '../../api/sceneApi';

export const PublishDialog: React.FC = () => {
  const scene = useSelector(selectScene);
  const isPublishing = useSelector(selectIsPublishing);
  const dispatch = useDispatch();
  
  const [version, setVersion] = useState(`v${scene.version + 1}`);
  const [changelog, setChangelog] = useState('');
  const [platforms, setPlatforms] = useState({
    web: true,
    mobile: false,
    quest: false,
    visionPro: false,
  });
  
  const [publishStep, setPublishStep] = useState(0);
  const [publishResult, setPublishResult] = useState<any>(null);

  const handlePublish = async () => {
    dispatch(setPublishing(true));
    setPublishStep(1);
    
    try {
      // Step 1: Validate
      setPublishStep(1);
      await validateScene(scene);
      
      // Step 2: Process assets
      setPublishStep(2);
      await processAssets(scene);
      
      // Step 3: Save to database
      setPublishStep(3);
      const result = await publishScene({
        sceneId: scene.id,
        version,
        changelog,
        platforms,
      });
      
      // Step 4: Deploy
      setPublishStep(4);
      await deployScene(result.sceneId, result.version);
      
      setPublishResult({
        success: true,
        url: result.publicUrl,
        qrCode: result.qrCode,
        embedCode: result.embedCode,
      });
      
      setPublishStep(5);
    } catch (error) {
      setPublishResult({
        success: false,
        error: error.message,
      });
    } finally {
      dispatch(setPublishing(false));
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <div className="bg-gray-900 rounded-lg w-full max-w-2xl">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-700">
          <h2 className="text-lg font-medium">Publish Scene</h2>
          <button className="text-gray-400 hover:text-white">
            ✕
          </button>
        </div>
        
        {/* Content */}
        <div className="p-6">
          {publishStep === 0 && (
            <>
              {/* Version */}
              <div className="mb-4">
                <label className="block text-sm text-gray-400 mb-1">Version</label>
                <input
                  type="text"
                  value={version}
                  onChange={(e) => setVersion(e.target.value)}
                  className="w-full bg-gray-800 border border-gray-700 rounded px-3 py-2"
                />
              </div>
              
              {/* Changelog */}
              <div className="mb-4">
                <label className="block text-sm text-gray-400 mb-1">Changelog</label>
                <textarea
                  value={changelog}
                  onChange={(e) => setChangelog(e.target.value)}
                  rows={3}
                  className="w-full bg-gray-800 border border-gray-700 rounded px-3 py-2"
                  placeholder="What changed in this version..."
                />
              </div>
              
              {/* Platforms */}
              <div className="mb-6">
                <label className="block text-sm text-gray-400 mb-2">Platforms</label>
                <div className="grid grid-cols-2 gap-3">
                  {Object.entries(platforms).map(([platform, enabled]) => (
                    <label
                      key={platform}
                      className={`flex items-center gap-3 p-3 rounded border cursor-pointer ${
                        enabled
                          ? 'border-blue-500 bg-blue-500/10'
                          : 'border-gray-700 hover:border-gray-600'
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={enabled}
                        onChange={(e) =>
                          setPlatforms({ ...platforms, [platform]: e.target.checked })
                        }
                        className="rounded"
                      />
                      <span className="capitalize">{platform}</span>
                    </label>
                  ))}
                </div>
              </div>
            </>
          )}
          
          {publishStep > 0 && publishStep <= 4 && (
            <div className="space-y-4">
              {['Validating', 'Processing Assets', 'Saving', 'Deploying'].map(
                (step, index) => (
                  <div key={step} className="flex items-center gap-3">
                    {publishStep > index + 1 ? (
                      <span className="text-green-500">✓</span>
                    ) : publishStep === index + 1 ? (
                      <span className="animate-spin">⟳</span>
                    ) : (
                      <span className="text-gray-600">○</span>
                    )}
                    <span className={publishStep >= index + 1 ? 'text-white' : 'text-gray-500'}>
                      {step}
                    </span>
                  </div>
                )
              )}
            </div>
          )}
          
          {publishStep === 5 && publishResult && (
            <div className="space-y-4">
              {publishResult.success ? (
                <>
                  <div className="text-green-500 text-center mb-4">
                    Scene published successfully!
                  </div>
                  
                  {/* Public URL */}
                  <div className="bg-gray-800 rounded p-3">
                    <label className="text-xs text-gray-400">Public URL</label>
                    <div className="flex items-center gap-2 mt-1">
                      <input
                        type="text"
                        value={publishResult.url}
                        readOnly
                        className="flex-1 bg-gray-700 rounded px-2 py-1 text-sm"
                      />
                      <button
                        onClick={() => navigator.clipboard.writeText(publishResult.url)}
                        className="px-3 py-1 bg-blue-600 rounded text-sm"
                      >
                        Copy
                      </button>
                    </div>
                  </div>
                  
                  {/* QR Code */}
                  <div className="flex justify-center">
                    <img
                      src={publishResult.qrCode}
                      alt="QR Code"
                      className="w-32 h-32"
                    />
                  </div>
                  
                  {/* Embed Code */}
                  <div className="bg-gray-800 rounded p-3">
                    <label className="text-xs text-gray-400">Embed Code</label>
                    <pre className="mt-1 text-xs text-gray-300 overflow-x-auto">
                      {publishResult.embedCode}
                    </pre>
                  </div>
                </>
              ) : (
                <div className="text-red-500 text-center">
                  Publish failed: {publishResult.error}
                </div>
              )}
            </div>
          )}
        </div>
        
        {/* Footer */}
        <div className="flex items-center justify-end gap-3 p-4 border-t border-gray-700">
          {publishStep === 0 && (
            <>
              <button className="px-4 py-2 bg-gray-700 rounded hover:bg-gray-600">
                Cancel
              </button>
              <button
                onClick={handlePublish}
                disabled={isPublishing}
                className="px-4 py-2 bg-blue-600 rounded hover:bg-blue-700 disabled:opacity-50"
              >
                Publish
              </button>
            </>
          )}
          
          {publishStep === 5 && (
            <button className="px-4 py-2 bg-blue-600 rounded hover:bg-blue-700">
              Done
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
```

---

## 11. Collaboration Editing

### 11.1 WebSocket-based Real-time Sync

```typescript
// collaboration/CollaborationManager.ts
import * as Y from 'yjs';
import { WebsocketProvider } from 'y-websocket';
import { Scene } from '../store/types';
import { deepClone } from '../utils/clone';

export class CollaborationManager {
  private ydoc: Y.Doc;
  private provider: WebsocketProvider;
  private yScene: Y.Map<any>;
  private awareness: any;
  
  private localUserId: string;
  private localUserName: string;
  private localUserColor: string;
  
  private onSceneChange?: (scene: Scene) => void;
  private onUsersChange?: (users: Collaborator[]) => void;
  private onCursorsChange?: (cursors: Map<string, CursorPosition>) => void;
  
  constructor(
    sceneId: string,
    userId: string,
    userName: string,
    serverUrl: string
  ) {
    this.localUserId = userId;
    this.localUserName = userName;
    this.localUserColor = this.generateUserColor();
    
    // Initialize Y.js document
    this.ydoc = new Y.Doc();
    
    // Connect to WebSocket server
    this.provider = new WebsocketProvider(
      serverUrl,
      `scene:${sceneId}`,
      this.ydoc
    );
    
    // Get shared scene map
    this.yScene = this.ydoc.getMap('scene');
    
    // Set up awareness (user presence)
    this.awareness = this.provider.awareness;
    this.awareness.setLocalState({
      user: {
        id: userId,
        name: userName,
        color: this.localUserColor,
      },
      cursor: null,
    });
    
    // Listen for changes
    this.yScene.observeDeep(this.handleYSceneChange.bind(this));
    this.awareness.on('change', this.handleAwarenessChange.bind(this));
  }
  
  // Push local changes to Y.js
  pushSceneChange(scene: Scene) {
    this.ydoc.transact(() => {
      this.yScene.set('data', JSON.stringify(scene));
      this.yScene.set('updatedAt', Date.now());
      this.yScene.set('updatedBy', this.localUserId);
    });
  }
  
  // Handle remote changes
  private handleYSceneChange() {
    const sceneData = this.yScene.get('data');
    const updatedBy = this.yScene.get('updatedBy');
    
    if (sceneData && updatedBy !== this.localUserId) {
      const scene = JSON.parse(sceneData) as Scene;
      this.onSceneChange?.(scene);
    }
  }
  
  // Handle awareness changes (user presence)
  private handleAwarenessChange() {
    const states = this.awareness.getStates();
    const users: Collaborator[] = [];
    const cursors = new Map<string, CursorPosition>();
    
    states.forEach((state: any, clientId: number) => {
      if (state.user) {
        users.push({
          id: state.user.id,
          name: state.user.name,
          color: state.user.color,
          avatar: '',
        });
        
        if (state.cursor && clientId !== this.awareness.clientID) {
          cursors.set(state.user.id, state.cursor);
        }
      }
    });
    
    this.onUsersChange?.(users);
    this.onCursorsChange?.(cursors);
  }
  
  // Update cursor position
  updateCursor(position: { x: number; y: number; nodeId: string | null }) {
    this.awareness.setLocalStateField('cursor', position);
  }
  
  // Lock node for editing
  lockNode(nodeId: string): boolean {
    const lockKey = `lock:${nodeId}`;
    const existingLock = this.yScene.get(lockKey);
    
    if (existingLock && existingLock !== this.localUserId) {
      return false; // Already locked by another user
    }
    
    this.yScene.set(lockKey, this.localUserId);
    return true;
  }
  
  // Unlock node
  unlockNode(nodeId: string) {
    const lockKey = `lock:${nodeId}`;
    const existingLock = this.yScene.get(lockKey);
    
    if (existingLock === this.localUserId) {
      this.yScene.delete(lockKey);
    }
  }
  
  // Check if node is locked
  isNodeLocked(nodeId: string): string | null {
    const lockKey = `lock:${nodeId}`;
    const lockOwner = this.yScene.get(lockKey);
    
    if (lockOwner && lockOwner !== this.localUserId) {
      return lockOwner;
    }
    
    return null;
  }
  
  // Event handlers
  setOnSceneChange(handler: (scene: Scene) => void) {
    this.onSceneChange = handler;
  }
  
  setOnUsersChange(handler: (users: Collaborator[]) => void) {
    this.onUsersChange = handler;
  }
  
  setOnCursorsChange(handler: (cursors: Map<string, CursorPosition>) => void) {
    this.onCursorsChange = handler;
  }
  
  // Disconnect
  disconnect() {
    this.awareness.setLocalState(null);
    this.provider.disconnect();
    this.ydoc.destroy();
  }
  
  private generateUserColor(): string {
    const colors = [
      '#f44336', '#e91e63', '#9c27b0', '#673ab7',
      '#3f51b5', '#2196f3', '#03a9f4', '#00bcd4',
      '#009688', '#4caf50', '#8bc34a', '#cddc39',
      '#ffc107', '#ff9800', '#ff5722',
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  }
}
```

### 11.2 Collaboration UI Components

```typescript
// components/Collaboration/CollaboratorAvatars.tsx
import React from 'react';
import { useSelector } from 'react-redux';
import { selectCollaborators } from '../../store/selectors';

export const CollaboratorAvatars: React.FC = () => {
  const collaborators = useSelector(selectCollaborators);
  
  return (
    <div className="flex items-center gap-2">
      <div className="flex -space-x-2">
        {collaborators.slice(0, 5).map((user) => (
          <div
            key={user.id}
            className="w-8 h-8 rounded-full border-2 border-gray-900 flex items-center justify-center text-xs font-medium"
            style={{ backgroundColor: user.color }}
            title={user.name}
          >
            {user.name.charAt(0).toUpperCase()}
          </div>
        ))}
        
        {collaborators.length > 5 && (
          <div className="w-8 h-8 rounded-full border-2 border-gray-900 bg-gray-700 flex items-center justify-center text-xs">
            +{collaborators.length - 5}
          </div>
        )}
      </div>
      
      <span className="text-sm text-gray-400">
        {collaborators.length} editing
      </span>
    </div>
  );
};

// components/Collaboration/UserCursor.tsx
import React from 'react';
import { CursorPosition } from '../../store/types';

interface UserCursorProps {
  user: Collaborator;
  position: CursorPosition;
}

export const UserCursor: React.FC<UserCursorProps> = ({ user, position }) => {
  return (
    <div
      className="absolute pointer-events-none z-50"
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
      }}
    >
      {/* Cursor arrow */}
      <svg
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        style={{ color: user.color }}
      >
        <path
          d="M5 3L19 12L12 14L9 21L5 3Z"
          fill="currentColor"
          stroke="white"
          strokeWidth="1"
        />
      </svg>
      
      {/* User name label */}
      <div
        className="absolute left-5 top-5 px-2 py-0.5 rounded text-xs text-white whitespace-nowrap"
        style={{ backgroundColor: user.color }}
      >
        {user.name}
      </div>
    </div>
  );
};
```

---

## 12. Keyboard Shortcuts

### 12.1 Shortcut Registry

```typescript
// hooks/useKeyboardShortcuts.ts
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { selectCanUndo, selectCanRedo, selectSelectedNodeId } from '../store/selectors';
import { undo, redo, removeNode, duplicateNode, toggleGrid, toggleSnap } from '../store/sceneSlice';

interface ShortcutMap {
  [key: string]: () => void;
}

export const useKeyboardShortcuts = () => {
  const dispatch = useDispatch();
  const canUndo = useSelector(selectCanUndo);
  const canRedo = useSelector(selectCanRedo);
  const selectedNodeId = useSelector(selectSelectedNodeId);

  useEffect(() => {
    const shortcuts: ShortcutMap = {
      'ctrl+s': () => {
        // Save
        dispatch({ type: 'scene/saveScene' });
      },
      'ctrl+z': () => {
        if (canUndo) dispatch(undo());
      },
      'ctrl+shift+z': () => {
        if (canRedo) dispatch(redo());
      },
      'delete': () => {
        if (selectedNodeId && selectedNodeId !== 'root') {
          dispatch(removeNode(selectedNodeId));
        }
      },
      'ctrl+d': () => {
        if (selectedNodeId) {
          dispatch(duplicateNode(selectedNodeId));
        }
      },
      'ctrl+g': () => {
        dispatch(toggleGrid());
      },
      'f': () => {
        // Focus on selected
        dispatch({ type: 'viewport/focusOnSelected' });
      },
      ' ': () => {
        // Toggle preview
        dispatch({ type: 'ui/togglePreview' });
      },
      'p': () => {
        // Publish
        dispatch({ type: 'ui/togglePublishDialog' });
      },
      'escape': () => {
        // Deselect
        dispatch({ type: 'scene/setSelectedNode', payload: null });
      },
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      // Don't trigger shortcuts when typing in inputs
      if (event.target instanceof HTMLInputElement || 
          event.target instanceof HTMLTextAreaElement) {
        return;
      }

      const key = [
        event.ctrlKey || event.metaKey ? 'ctrl' : '',
        event.shiftKey ? 'shift' : '',
        event.altKey ? 'alt' : '',
        event.key.toLowerCase(),
      ]
        .filter(Boolean)
        .join('+');

      if (shortcuts[key]) {
        event.preventDefault();
        shortcuts[key]();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [dispatch, canUndo, canRedo, selectedNodeId]);
};
```

### 12.2 Shortcut Reference Table

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+S` | Save | Save current scene |
| `Ctrl+Z` | Undo | Undo last action |
| `Ctrl+Shift+Z` | Redo | Redo last undone action |
| `Delete` | Delete | Remove selected node |
| `Ctrl+D` | Duplicate | Duplicate selected node |
| `Ctrl+G` | Toggle Grid | Show/hide viewport grid |
| `F` | Focus | Focus camera on selected |
| `Space` | Preview | Toggle preview mode |
| `P` | Publish | Open publish dialog |
| `Escape` | Deselect | Clear selection |
| `Ctrl+A` | Select All | Select all nodes |
| `Ctrl+C` | Copy | Copy selected node |
| `Ctrl+V` | Paste | Paste copied node |
| `Ctrl+X` | Cut | Cut selected node |
| `Ctrl+Shift+G` | Group | Group selected nodes |
| `Ctrl+Shift+U` | Ungroup | Ungroup selected |

---

## 13. API Integration

### 13.1 API Endpoints

```typescript
// api/sceneApi.ts
import axios from 'axios';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'https://api.xrvista.com';

export interface SceneResponse {
  id: string;
  name: string;
  description: string;
  graph: string; // JSON stringified scene graph
  version: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  status: 'draft' | 'published' | 'archived';
}

export interface PublishResponse {
  sceneId: string;
  version: string;
  publicUrl: string;
  qrCode: string;
  embedCode: string;
  cdnUrls: string[];
}

// Scene CRUD
export const sceneApi = {
  // Get scene graph
  async getSceneGraph(sceneId: string, token: string): Promise<SceneResponse> {
    const response = await axios.get(`${API_BASE}/api/v1/scenes/${sceneId}/graph`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },

  // Save scene graph
  async saveSceneGraph(
    sceneId: string,
    graph: string,
    token: string
  ): Promise<void> {
    await axios.put(
      `${API_BASE}/api/v1/scenes/${sceneId}/graph`,
      { graph },
      { headers: { Authorization: `Bearer ${token}` } }
    );
  },

  // Publish scene
  async publishScene(
    sceneId: string,
    data: {
      version: string;
      changelog: string;
      platforms: Record<string, boolean>;
    },
    token: string
  ): Promise<PublishResponse> {
    const response = await axios.post(
      `${API_BASE}/api/v1/scenes/${sceneId}/publish`,
      data,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },

  // Generate preview
  async generatePreview(
    sceneId: string,
    token: string
  ): Promise<{ previewUrl: string }> {
    const response = await axios.post(
      `${API_BASE}/api/v1/scenes/${sceneId}/preview`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },

  // Get scene versions
  async getSceneVersions(
    sceneId: string,
    token: string
  ): Promise<{ versions: Array<{ version: string; createdAt: string; publishedBy: string }> }> {
    const response = await axios.get(
      `${API_BASE}/api/v1/scenes/${sceneId}/versions`,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },

  // Revert to version
  async revertToVersion(
    sceneId: string,
    version: string,
    token: string
  ): Promise<void> {
    await axios.post(
      `${API_BASE}/api/v1/scenes/${sceneId}/revert`,
      { version },
      { headers: { Authorization: `Bearer ${token}` } }
    );
  },
};

// Asset management
export const assetApi = {
  // Upload asset
  async uploadAsset(
    file: File,
    tenantId: string,
    onProgress: (progress: number) => void
  ): Promise<{ id: string; url: string; thumbnailUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('tenantId', tenantId);

    const response = await axios.post(`${API_BASE}/api/v1/assets/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (progressEvent) => {
        const progress = progressEvent.loaded / (progressEvent.total || 1);
        onProgress(Math.round(progress * 100));
      },
    });

    return response.data;
  },

  // Get asset list
  async getAssets(
    tenantId: string,
    filters?: { type?: string; tags?: string[] }
  ): Promise<{ assets: Asset[] }> {
    const response = await axios.get(`${API_BASE}/api/v1/assets`, {
      params: { tenantId, ...filters },
    });
    return response.data;
  },

  // Delete asset
  async deleteAsset(assetId: string): Promise<void> {
    await axios.delete(`${API_BASE}/api/v1/assets/${assetId}`);
  },
};
```

### 13.2 Scene Graph JSON Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Scene Graph",
  "type": "object",
  "required": ["id", "name", "nodes"],
  "properties": {
    "id": {
      "type": "string",
      "format": "uuid"
    },
    "name": {
      "type": "string",
      "minLength": 1,
      "maxLength": 200
    },
    "description": {
      "type": "string",
      "maxLength": 2000
    },
    "version": {
      "type": "integer",
      "minimum": 1
    },
    "settings": {
      "type": "object",
      "properties": {
        "backgroundColor": { "type": "string", "pattern": "^#[0-9a-fA-F]{6}$" },
        "ambientLight": { "type": "number", "minimum": 0, "maximum": 2 },
        "backgroundType": { "type": "string", "enum": ["color", "360_image", "360_video", "gradient"] }
      }
    },
    "nodes": {
      "type": "array",
      "items": {
        "$ref": "#/definitions/SceneNode"
      }
    }
  },
  "definitions": {
    "SceneNode": {
      "type": "object",
      "required": ["id", "type", "name"],
      "properties": {
        "id": { "type": "string" },
        "type": { "type": "string", "enum": ["scene", "background", "hotspot_info", "hotspot_nav", "hotspot_media", "text", "image", "video", "audio", "model_3d", "ui_panel", "light", "group"] },
        "name": { "type": "string" },
        "visible": { "type": "boolean", "default": true },
        "locked": { "type": "boolean", "default": false },
        "parentId": { "type": ["string", "null"] },
        "children": { "type": "array", "items": { "type": "string" } },
        "transform": { "$ref": "#/definitions/Transform" },
        "properties": { "type": "object" },
        "interactions": {
          "type": "array",
          "items": { "$ref": "#/definitions/Interaction" }
        }
      }
    },
    "Transform": {
      "type": "object",
      "properties": {
        "position": { "$ref": "#/definitions/Vector3" },
        "rotation": { "$ref": "#/definitions/Vector3" },
        "scale": { "$ref": "#/definitions/Vector3" }
      }
    },
    "Vector3": {
      "type": "object",
      "properties": {
        "x": { "type": "number" },
        "y": { "type": "number" },
        "z": { "type": "number" }
      }
    },
    "Interaction": {
      "type": "object",
      "properties": {
        "id": { "type": "string" },
        "trigger": { "type": "string", "enum": ["click", "hover", "gaze", "enter_scene"] },
        "action": { "type": "string", "enum": ["navigate", "play_media", "show_panel", "hide_panel", "toggle_visibility", "animate"] },
        "parameters": { "type": "object" }
      }
    }
  }
}
```

---

## 14. Testing Strategy

### 14.1 Test Coverage Requirements

| Component | Unit Tests | Integration Tests | E2E Tests |
|-----------|-----------|-------------------|-----------|
| Redux Store | 100% | - | - |
| Node Registry | 100% | - | - |
| History Manager | 100% | - | - |
| Viewport | 90% | 80% | 70% |
| Drag & Drop | 90% | 80% | 70% |
| Properties Panel | 95% | 85% | - |
| Scene Tree | 95% | 85% | 70% |
| Collaboration | 90% | 80% | 60% |
| Publishing | 90% | 85% | 70% |

### 14.2 Test Examples

```typescript
// __tests__/store/sceneSlice.test.ts
import { sceneReducer, addNode, removeNode, undo, redo } from '../../store/sceneSlice';
import { Scene, SceneNode } from '../../store/types';

describe('sceneSlice', () => {
  const initialState = {
    scene: {
      id: 'test',
      name: 'Test Scene',
      nodes: [
        {
          id: 'root',
          type: 'scene',
          name: 'Root',
          children: [],
          // ... other fields
        },
      ],
      // ... other fields
    },
    selectedNodeId: null,
    history: { past: [], future: [], maxSize: 50 },
    isDirty: false,
    isSaving: false,
    isPublishing: false,
  };

  it('should add a node', () => {
    const newNode: SceneNode = {
      id: 'node-1',
      type: 'hotspot_info',
      name: 'Test Hotspot',
      visible: true,
      locked: false,
      parentId: 'root',
      children: [],
      transform: {
        position: { x: 0, y: 0, z: -5 },
        rotation: { x: 0, y: 0, z: 0 },
        scale: { x: 1, y: 1, z: 1 },
      },
      properties: { title: 'Test' },
      interactions: [],
      metadata: {},
    };

    const action = addNode({ node: newNode });
    const newState = sceneReducer(initialState, action);

    expect(newState.scene.nodes).toHaveLength(2);
    expect(newState.scene.nodes[1].id).toBe('node-1');
    expect(newState.isDirty).toBe(true);
  });

  it('should remove a node and its children', () => {
    const stateWithChild = {
      ...initialState,
      scene: {
        ...initialState.scene,
        nodes: [
          initialState.scene.nodes[0],
          {
            id: 'node-1',
            type: 'hotspot_info',
            name: 'Parent',
            parentId: 'root',
            children: ['node-2'],
          },
          {
            id: 'node-2',
            type: 'text',
            name: 'Child',
            parentId: 'node-1',
            children: [],
          },
        ],
      },
    };

    const action = removeNode('node-1');
    const newState = sceneReducer(stateWithChild, action);

    expect(newState.scene.nodes).toHaveLength(1);
    expect(newState.scene.nodes.find(n => n.id === 'node-1')).toBeUndefined();
    expect(newState.scene.nodes.find(n => n.id === 'node-2')).toBeUndefined();
  });

  it('should undo and redo', () => {
    const stateWithHistory = {
      ...initialState,
      history: {
        past: [{ scene: initialState.scene, timestamp: Date.now(), action: 'test' }],
        future: [],
        maxSize: 50,
      },
    };

    const undoState = sceneReducer(stateWithHistory, undo());
    expect(undoState.history.past).toHaveLength(0);
    expect(undoState.history.future).toHaveLength(1);

    const redoState = sceneReducer(undoState, redo());
    expect(redoState.history.past).toHaveLength(1);
    expect(redoState.history.future).toHaveLength(0);
  });
});

// __tests__/components/Viewport.test.tsx
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { Viewport } from '../../components/Viewport';
import sceneReducer from '../../store/sceneSlice';

const createTestStore = () => configureStore({
  reducer: { scene: sceneReducer },
});

describe('Viewport', () => {
  it('renders canvas element', () => {
    const store = createTestStore();
    render(
      <Provider store={store}>
        <Viewport onDrop={jest.fn()} />
      </Provider>
    );
    
    expect(screen.getByRole('canvas')).toBeInTheDocument();
  });

  it('calls onDrop when asset is dropped', async () => {
    const onDrop = jest.fn();
    const store = createTestStore();
    
    render(
      <Provider store={store}>
        <Viewport onDrop={onDrop} />
      </Provider>
    );
    
    const canvas = screen.getByRole('canvas');
    
    // Simulate drop event
    const dropEvent = new Event('drop', { bubbles: true });
    Object.defineProperty(dropEvent, 'clientX', { value: 100 });
    Object.defineProperty(dropEvent, 'clientY', { value: 100 });
    Object.defineProperty(dropEvent, 'dataTransfer', {
      value: {
        getData: () => JSON.stringify({ type: 'ASSET', asset: { id: '1' } }),
      },
    });
    
    fireEvent(canvas, dropEvent);
    
    expect(onDrop).toHaveBeenCalled();
  });
});
```

---

## 15. Performance Optimization

### 15.1 Performance Budget

| Metric | Target | Measurement |
|--------|--------|-------------|
| Initial Load | < 2s | Time to Interactive |
| Scene Save | < 500ms | API response time |
| Undo/Redo | < 16ms | Frame time |
| Viewport FPS | 60fps | RequestAnimationFrame |
| Memory Usage | < 500MB | Chrome DevTools |
| Bundle Size | < 2MB | Webpack bundle analyzer |

### 15.2 Optimization Techniques

```typescript
// optimizations/ViewportOptimizer.ts
import * as THREE from 'three';

export class ViewportOptimizer {
  private renderer: THREE.WebGLRenderer;
  private frustum = new THREE.Frustum();
  private camera: THREE.PerspectiveCamera;
  
  constructor(renderer: THREE.WebGLRenderer, camera: THREE.PerspectiveCamera) {
    this.renderer = renderer;
    this.camera = camera;
  }
  
  // Frustum culling - only render visible objects
  frustumCull(objects: THREE.Object3D[]): THREE.Object3D[] {
    const cameraMatrix = new THREE.Matrix4().multiplyMatrices(
      this.camera.projectionMatrix,
      this.camera.matrixWorldInverse
    );
    this.frustum.setFromProjectionMatrix(cameraMatrix);
    
    return objects.filter(obj => {
      const boundingSphere = new THREE.Sphere();
      if (obj instanceof THREE.Mesh && obj.geometry.boundingSphere) {
        boundingSphere.copy(obj.geometry.boundingSphere);
        boundingSphere.applyMatrix4(obj.matrixWorld);
        return this.frustum.intersectsSphere(boundingSphere);
      }
      return true;
    });
  }
  
  // Level of Detail management
  selectLOD(distance: number, lodLevels: THREE.Object3D[]): THREE.Object3D {
    if (distance < 10) return lodLevels[0]; // High detail
    if (distance < 30) return lodLevels[1]; // Medium detail
    return lodLevels[2]; // Low detail
  }
  
  // Texture compression
  getOptimalTextureFormat(): string {
    const gl = this.renderer.getContext();
    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
    const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);
    
    if (renderer.includes('Adreno') || renderer.includes('Mali')) {
      return 'astc'; // Mobile GPU
    }
    return 'bc7'; // Desktop GPU
  }
  
  // Instanced rendering for repeated objects
  createInstancedMesh(
    geometry: THREE.BufferGeometry,
    material: THREE.Material,
    count: number
  ): THREE.InstancedMesh {
    const mesh = new THREE.InstancedMesh(geometry, material, count);
    mesh.instanceMatrix.setUsage(THREE.DynamicDrawUsage);
    return mesh;
  }
  
  // Object pooling for frequently created/destroyed objects
  private objectPool = new Map<string, THREE.Object3D[]>();
  
  acquireObject(type: string): THREE.Object3D {
    const pool = this.objectPool.get(type) || [];
    if (pool.length > 0) {
      return pool.pop()!;
    }
    return this.createObject(type);
  }
  
  releaseObject(type: string, obj: THREE.Object3D) {
    const pool = this.objectPool.get(type) || [];
    pool.push(obj);
    this.objectPool.set(type, pool);
  }
}
```

---

## 16. Deployment

### 16.1 Build Configuration

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    react(),
    visualizer({
      open: true,
      gzipSize: true,
    }),
  ],
  build: {
    target: 'es2020',
    minify: 'terser',
    rollupOptions: {
      output: {
        manualChunks: {
          three: ['three'],
          redux: ['@reduxjs/toolkit', 'react-redux'],
          dnd: ['react-dnd', 'react-dnd-html5-backend'],
          yjs: ['yjs', 'y-websocket'],
        },
      },
    },
    chunkSizeWarningLimit: 1000,
  },
  optimizeDeps: {
    include: ['three', '@reduxjs/toolkit'],
  },
});
```

### 16.2 Deployment Pipeline

```
┌─────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT PIPELINE                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────┐     ┌─────────┐     ┌─────────┐               │
│  │  Push   │────▶│  Build  │────▶│  Test   │               │
│  │  Code   │     │  App    │     │  Suite  │               │
│  └─────────┘     └─────────┘     └────┬────┘               │
│                                       │                     │
│                                       ▼                     │
│                                ┌─────────────┐              │
│                                │  Deploy to  │              │
│                                │  Staging    │              │
│                                └──────┬──────┘              │
│                                       │                     │
│                                       ▼                     │
│                                ┌─────────────┐              │
│                                │  QA Review  │              │
│                                └──────┬──────┘              │
│                                       │                     │
│                                       ▼                     │
│                                ┌─────────────┐              │
│                                │  Deploy to  │              │
│                                │  Production │              │
│                                └──────┬──────┘              │
│                                       │                     │
│                                       ▼                     │
│                                ┌─────────────┐              │
│                                │  Monitor    │              │
│                                │  Metrics    │              │
│                                └─────────────┘              │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Appendix A: Glossary

| Term | Definition |
|------|-----------|
| Scene Graph | Tree structure representing all nodes in a VR scene |
| Node | Individual element in a scene (hotspot, model, text, etc.) |
| Transform | Position, rotation, and scale of a node |
| Hotspot | Interactive element in 360° scene |
| CRDT | Conflict-free Replicated Data Type for collaboration |
| FBO | Frame Buffer Object for offscreen rendering |
| LOD | Level of Detail for performance optimization |
| WebXR | Web API for VR/AR experiences |
| glTF | GL Transmission Format for 3D models |

## Appendix B: API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/scenes/{id}/graph` | Get scene graph |
| PUT | `/api/v1/scenes/{id}/graph` | Save scene graph |
| POST | `/api/v1/scenes/{id}/publish` | Publish scene |
| POST | `/api/v1/scenes/{id}/preview` | Generate preview |
| GET | `/api/v1/scenes/{id}/versions` | Get version history |
| POST | `/api/v1/scenes/{id}/revert` | Revert to version |
| POST | `/api/v1/assets/upload` | Upload asset |
| GET | `/api/v1/assets` | List assets |
| DELETE | `/api/v1/assets/{id}` | Delete asset |

## Appendix C: File Structure

```
scene-builder/
├── src/
│   ├── api/
│   │   ├── sceneApi.ts
│   │   └── assetApi.ts
│   ├── components/
│   │   ├── AssetPanel/
│   │   ├── Viewport/
│   │   ├── PropertiesPanel/
│   │   ├── SceneTreePanel/
│   │   ├── Toolbar/
│   │   ├── TopBar/
│   │   ├── PreviewModal/
│   │   └── PublishDialog/
│   ├── hooks/
│   │   ├── useKeyboardShortcuts.ts
│   │   └── useViewportControls.ts
│   ├── registry/
│   │   └── nodeRegistry.ts
│   ├── store/
│   │   ├── store.ts
│   │   ├── sceneSlice.ts
│   │   ├── selectors.ts
│   │   └── types.ts
│   ├── three/
│   │   ├── TransformGizmo.ts
│   │   ├── GridHelper.ts
│   │   └── SelectionOutline.ts
│   ├── collaboration/
│   │   └── CollaborationManager.ts
│   ├── utils/
│   │   ├── id.ts
│   │   └── clone.ts
│   ├── SceneBuilder.tsx
│   └── main.tsx
├── public/
├── tests/
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

*Document maintained by XR Platform Team*
*Last updated: 2026-06-05*
