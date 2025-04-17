import React, { createContext, useContext, useState, ReactNode } from 'react';

interface UnsavedChangesContextType {
  hasUnsavedChanges: boolean;
  setHasUnsavedChanges: (value: boolean) => void;
  showUnsavedChangesDialog: (callback: () => void) => void;
}

const UnsavedChangesContext = createContext<UnsavedChangesContextType | undefined>(undefined);

export const useUnsavedChanges = () => {
  const context = useContext(UnsavedChangesContext);
  if (context === undefined) {
    throw new Error('useUnsavedChanges must be used within an UnsavedChangesProvider');
  }
  return context;
};

interface UnsavedChangesProviderProps {
  children: ReactNode;
}

export const UnsavedChangesProvider: React.FC<UnsavedChangesProviderProps> = ({ children }) => {
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [pendingCallback, setPendingCallback] = useState<(() => void) | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  const showUnsavedChangesDialog = (callback: () => void) => {
    setPendingCallback(() => callback);
    setDialogOpen(true);
  };

  const handleStayOnPage = () => {
    setDialogOpen(false);
    setPendingCallback(null);
  };

  const handleLeavePage = () => {
    setDialogOpen(false);
    if (pendingCallback) {
      pendingCallback();
      setPendingCallback(null);
    }
  };

  return (
    <UnsavedChangesContext.Provider 
      value={{ 
        hasUnsavedChanges, 
        setHasUnsavedChanges,
        showUnsavedChangesDialog
      }}
    >
      {children}
      {dialogOpen && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
          }}
        >
          <div
            style={{
              backgroundColor: 'rgba(30, 30, 30, 0.95)',
              backdropFilter: 'blur(10px)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              borderRadius: '8px',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.5)',
              padding: '24px',
              maxWidth: '400px',
              width: '100%',
            }}
          >
            <h3 style={{ color: '#fff', marginTop: 0, borderBottom: '1px solid rgba(255, 255, 255, 0.1)', paddingBottom: '16px' }}>
              Unsaved Changes
            </h3>
            <p style={{ color: '#fff' }}>
              You have unsaved changes. If you leave this page, your changes will be lost. Do you want to continue?
            </p>
            <div style={{ 
              display: 'flex', 
              justifyContent: 'flex-end', 
              gap: '16px', 
              marginTop: '24px',
              borderTop: '1px solid rgba(255, 255, 255, 0.1)',
              paddingTop: '16px'
            }}>
              <button
                onClick={handleStayOnPage}
                style={{
                  backgroundColor: 'transparent',
                  border: 'none',
                  color: '#4D9FFF',
                  cursor: 'pointer',
                  padding: '8px 16px',
                  borderRadius: '4px',
                }}
              >
                Stay on Page
              </button>
              <button
                onClick={handleLeavePage}
                style={{
                  backgroundColor: 'transparent',
                  border: 'none',
                  color: '#00E676',
                  cursor: 'pointer',
                  padding: '8px 16px',
                  borderRadius: '4px',
                }}
              >
                Leave Page
              </button>
            </div>
          </div>
        </div>
      )}
    </UnsavedChangesContext.Provider>
  );
}; 