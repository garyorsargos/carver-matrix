import React, { useState } from "react";
import { Box, Button } from "@mui/material";
import LoginRegisterForm from "../components/LoginRegisterForm";

export const Register: React.FC = () => {
  const [mode, setMode] = useState<"login" | "register">("register");

  const handleToggleMode = () => {
    setMode((prevMode) => (prevMode === "login" ? "register" : "login"));
  };

  const handleSubmit = (data: {
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }) => {
    console.log("Form submitted:", data);
    // TODO: Add logic for form submission
  };

  return (
    <Box sx={{ textAlign: "center", marginTop: 5 }}>
      <LoginRegisterForm mode={mode} onSubmit={handleSubmit} />
      <Button onClick={handleToggleMode} sx={{ marginTop: 2 }}>
        {mode === "login" ? "Register New User" : "Login"}
      </Button>
    </Box>
  );
};

export default Register;
