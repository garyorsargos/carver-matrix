import React, { useReducer, useState } from "react";
import {
  Box,
  Button,
  TextField,
  Typography,
  Card,
  CircularProgress,
  Alert,
} from "@mui/material";

type LoginRegisterFormProps = {
  mode: "login" | "register";
  onSubmit: (data: {
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }) => void;
};

type FormState = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
};

type ActionType =
  | { type: "SET_FIELD"; field: keyof FormState; value: string }
  | { type: "RESET" };

const initialState: FormState = {
  firstName: "",
  lastName: "",
  email: "",
  password: "",
};

const formReducer = (state: FormState, action: ActionType): FormState => {
  switch (action.type) {
    case "SET_FIELD":
      return { ...state, [action.field]: action.value };
    case "RESET":
      return initialState;
    default:
      return state;
  }
};

const LoginRegisterForm: React.FC<LoginRegisterFormProps> = ({
  mode,
  onSubmit,
}) => {
  const [state, dispatch] = useReducer(formReducer, initialState);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    dispatch({
      type: "SET_FIELD",
      field: e.target.name as keyof FormState,
      value: e.target.value,
    });
  };

  const validateEmail = (email: string) => /\S+@\S+\.\S+/.test(email);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!validateEmail(state.email)) {
      setError("Please enter a valid email address.");
      return;
    }
    if (state.password.length < 6) {
      setError("Password should be at least 6 characters long.");
      return;
    }

    setLoading(true);
    try {
      await onSubmit(state);
      dispatch({ type: "RESET" });
    } catch (err) {
      setError("An error occurred. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card sx={{ padding: 4, maxWidth: 400, margin: "auto" }}>
      <Typography variant="h4" sx={{ marginBottom: 2 }}>
        {mode === "login" ? "Login" : "Register"}
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ display: "flex", flexDirection: "column", gap: 2 }}
      >
        {mode === "register" && (
          <>
            <TextField
              label="First Name"
              name="firstName"
              value={state.firstName}
              onChange={handleChange}
              required
            />
            <TextField
              label="Last Name"
              name="lastName"
              value={state.lastName}
              onChange={handleChange}
              required
            />
          </>
        )}
        <TextField
          label="Email"
          name="email"
          type="email"
          value={state.email}
          onChange={handleChange}
          required
          error={Boolean(error) && !validateEmail(state.email)}
          helperText={
            error && !validateEmail(state.email) ? "Invalid email format" : ""
          }
        />
        <TextField
          label="Password"
          name="password"
          type="password"
          value={state.password}
          onChange={handleChange}
          required
          error={Boolean(error) && state.password.length < 6}
          helperText={
            error && state.password.length < 6 ? "Minimum 6 characters" : ""
          }
        />
        <Button
          type="submit"
          variant="contained"
          color="primary"
          disabled={loading}
        >
          {loading ? (
            <CircularProgress size={24} />
          ) : mode === "login" ? (
            "Login"
          ) : (
            "Register"
          )}
        </Button>
      </Box>
    </Card>
  );
};

export default LoginRegisterForm;
